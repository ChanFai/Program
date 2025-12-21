package com.example.documentmanager.service;

import com.example.documentmanager.entity.Document;
import com.example.documentmanager.entity.DocumentChunk;
import com.example.documentmanager.repository.DocumentChunkRepository;
import com.example.documentmanager.repository.DocumentRepository;
import com.example.documentmanager.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.chunk-size:5242880}")
    private Long chunkSize; // 默认5MB

    private static final String CHUNK_DIR = "chunks";

    @Override
    public Document saveDocument(MultipartFile file) throws IOException {
        // 创建存储目录
        String datePath = FileUtil.getDatePath();
        String fullPath = uploadDir + File.separator + datePath;
        FileUtil.createDirIfNotExists(fullPath);

        // 生成存储文件名
        String storedFilename = FileUtil.generateStoredFilename(file.getOriginalFilename());

        // 计算文件MD5值
        String fileHash = FileUtil.calculateMD5(file.getInputStream());

        // 检查是否已存在相同内容的文件（同名同内容处理）
        Optional<Document> sameNameAndContent = documentRepository.findByFilenameAndFileHash(
            file.getOriginalFilename(), fileHash);
        
        if (sameNameAndContent.isPresent()) {
            // 同名同内容：复用文件，创建新记录，版本号+1
            Document existingDoc = sameNameAndContent.get();
            Document newDoc = new Document();
            newDoc.setFilename(file.getOriginalFilename());
            newDoc.setStoredFilename(existingDoc.getStoredFilename());
            newDoc.setFileSize(existingDoc.getFileSize());
            newDoc.setFileHash(fileHash);
            newDoc.setContentType(file.getContentType());
            newDoc.setUploadTime(new Date());
            newDoc.setDownloadCount(0);
            newDoc.setVersion(existingDoc.getVersion() + 1);
            newDoc.setUploadStatus("completed");
            return documentRepository.save(newDoc);
        }
        
        // 检查是否存在相同内容但不同名的文件
        List<Document> existingDocuments = documentRepository.findByFileHash(fileHash);
        if (!existingDocuments.isEmpty()) {
            // 同名不同内容：作为新文件保存（MD5不同说明内容不同）
            // 这里existingDocuments实际上应该是空的，因为上面已经检查过同名同内容
            // 但为了代码健壮性，保留这个检查
        }
        
        // 同名不同内容或全新文件：正常保存
        {
            // 保存文件到磁盘
            Path filePath = Paths.get(fullPath, storedFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 创建文档记录
            Document document = new Document();
            document.setFilename(file.getOriginalFilename());
            document.setStoredFilename(datePath + File.separator + storedFilename);
            document.setFileSize(file.getSize());
            document.setFileHash(fileHash);
            document.setContentType(file.getContentType());
            document.setUploadTime(new Date());
            document.setDownloadCount(0);
            document.setVersion(1);
            document.setUploadStatus("completed");

            return documentRepository.save(document);
        }
    }

    @Override
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    @Override
    public Document getDocumentById(Long id) {
        Optional<Document> document = documentRepository.findById(id);
        return document.orElse(null);
    }

    @Override
    public void deleteDocument(Long id) {
        Document document = getDocumentById(id);
        if (document != null) {
            // 删除文件
            String storedFilename = document.getStoredFilename();
            Path filePath = Paths.get(uploadDir, storedFilename);
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // 记录日志但不中断删除操作
                e.printStackTrace();
            }

            // 删除数据库记录
            documentRepository.deleteById(id);
        }
    }

    @Override
    public void downloadDocument(Long id, HttpServletResponse response) throws IOException {
        Document document = getDocumentById(id);
        if (document == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 增加下载次数
        document.setDownloadCount(document.getDownloadCount() + 1);
        documentRepository.save(document);

        // 设置响应头
        response.setContentType(document.getContentType());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + document.getFilename() + "\"");
        response.setContentLengthLong(document.getFileSize());

        // 写入文件内容
        Path filePath = Paths.get(uploadDir, document.getStoredFilename());
        if (Files.exists(filePath)) {
            try (InputStream inputStream = Files.newInputStream(filePath);
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    public Map<String, Object> initChunkUpload(String filename, String fileHash, Long fileSize, Integer totalChunks) throws IOException {
        Map<String, Object> result = new HashMap<>();
        
        // 检查是否已存在同名同内容的文件
        Optional<Document> existingDoc = documentRepository.findByFilenameAndFileHash(filename, fileHash);
        if (existingDoc.isPresent()) {
            // 同名同内容，直接返回已存在的文档
            result.put("exists", true);
            result.put("document", existingDoc.get());
            result.put("uploadedChunks", new ArrayList<>());
            return result;
        }
        
        // 检查是否有未完成的上传（断点续传）
        Optional<Document> uploadingDoc = documentRepository.findByFileHashAndUploadStatus(fileHash, "uploading");
        if (uploadingDoc.isPresent()) {
            Document doc = uploadingDoc.get();
            List<DocumentChunk> uploadedChunks = documentChunkRepository
                .findByDocumentIdAndUploadStatusOrderByChunkIndex(doc.getId(), "completed");
            List<Integer> chunkIndices = uploadedChunks.stream()
                .map(DocumentChunk::getChunkIndex)
                .collect(Collectors.toList());
            
            result.put("exists", false);
            result.put("resume", true);
            result.put("documentId", doc.getId());
            result.put("uploadedChunks", chunkIndices);
            return result;
        }
        
        // 创建新的上传记录
        Document document = new Document();
        document.setFilename(filename);
        document.setFileHash(fileHash);
        document.setFileSize(fileSize);
        document.setTotalChunks(totalChunks);
        document.setUploadedChunks(0);
        document.setUploadStatus("uploading");
        document.setUploadTime(new Date());
        document.setVersion(1);
        document.setDownloadCount(0);
        // 设置临时存储文件名（分片上传时使用临时路径，合并后会更新）
        document.setStoredFilename(CHUNK_DIR + File.separator + fileHash + File.separator + "temp");
        document.setContentType("application/octet-stream"); // 临时设置，合并时会更新
        
        // 创建分片存储目录
        String chunkDir = uploadDir + File.separator + CHUNK_DIR + File.separator + fileHash;
        FileUtil.createDirIfNotExists(chunkDir);
        
        document = documentRepository.save(document);
        
        result.put("exists", false);
        result.put("resume", false);
        result.put("documentId", document.getId());
        result.put("uploadedChunks", new ArrayList<>());
        return result;
    }

    @Override
    public Map<String, Object> uploadChunk(String fileHash, Integer chunkIndex, MultipartFile chunk) throws IOException {
        Map<String, Object> result = new HashMap<>();
        
        // 查找上传中的文档
        Optional<Document> docOpt = documentRepository.findByFileHashAndUploadStatus(fileHash, "uploading");
        if (!docOpt.isPresent()) {
            result.put("success", false);
            result.put("message", "未找到上传记录");
            return result;
        }
        
        Document document = docOpt.get();
        
        // 检查分片是否已存在
        Optional<DocumentChunk> existingChunk = documentChunkRepository
            .findByDocumentIdAndChunkIndex(document.getId(), chunkIndex);
        
        if (existingChunk.isPresent() && "completed".equals(existingChunk.get().getUploadStatus())) {
            // 分片已存在，跳过
            result.put("success", true);
            result.put("message", "分片已存在");
            result.put("skipped", true);
            return result;
        }
        
        // 保存分片
        String chunkDir = uploadDir + File.separator + CHUNK_DIR + File.separator + fileHash;
        FileUtil.createDirIfNotExists(chunkDir);
        String chunkFilename = chunkIndex + ".chunk";
        Path chunkPath = Paths.get(chunkDir, chunkFilename);
        
        // 保存分片文件
        Files.copy(chunk.getInputStream(), chunkPath, StandardCopyOption.REPLACE_EXISTING);
        
        // 计算分片MD5（从保存的文件计算）
        String chunkHash = FileUtil.calculateMD5(Files.newInputStream(chunkPath));
        
        // 保存分片记录
        DocumentChunk documentChunk;
        if (existingChunk.isPresent()) {
            documentChunk = existingChunk.get();
        } else {
            documentChunk = new DocumentChunk();
            documentChunk.setDocumentId(document.getId());
            documentChunk.setChunkIndex(chunkIndex);
        }
        
        documentChunk.setChunkSize(chunk.getSize());
        documentChunk.setChunkHash(chunkHash);
        documentChunk.setStoredPath(chunkPath.toString());
        documentChunk.setUploadStatus("completed");
        documentChunk.setUploadTime(new Date());
        documentChunkRepository.save(documentChunk);
        
        // 更新文档的上传进度
        document.setUploadedChunks(document.getUploadedChunks() + 1);
        documentRepository.save(document);
        
        result.put("success", true);
        result.put("message", "分片上传成功");
        result.put("uploadedChunks", document.getUploadedChunks());
        result.put("totalChunks", document.getTotalChunks());
        return result;
    }

    @Override
    public Document mergeChunks(String fileHash, String filename, String contentType) throws IOException {
        // 查找上传中的文档
        Optional<Document> docOpt = documentRepository.findByFileHashAndUploadStatus(fileHash, "uploading");
        if (!docOpt.isPresent()) {
            throw new IOException("未找到上传记录");
        }
        
        Document document = docOpt.get();
        
        // 获取所有分片
        List<DocumentChunk> chunks = documentChunkRepository
            .findByDocumentIdOrderByChunkIndex(document.getId());
        
        if (chunks.size() != document.getTotalChunks()) {
            throw new IOException("分片数量不完整");
        }
        
        // 创建存储目录
        String datePath = FileUtil.getDatePath();
        String fullPath = uploadDir + File.separator + datePath;
        FileUtil.createDirIfNotExists(fullPath);
        
        // 生成存储文件名
        String storedFilename = FileUtil.generateStoredFilename(filename);
        Path mergedFilePath = Paths.get(fullPath, storedFilename);
        
        // 合并分片
        try (FileChannel outChannel = FileChannel.open(mergedFilePath, 
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            
            for (DocumentChunk chunk : chunks) {
                Path chunkPath = Paths.get(chunk.getStoredPath());
                try (FileChannel inChannel = FileChannel.open(chunkPath, StandardOpenOption.READ)) {
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                }
            }
        }
        
        // 验证合并后的文件MD5
        String mergedFileHash = FileUtil.calculateMD5(Files.newInputStream(mergedFilePath));
        if (!fileHash.equals(mergedFileHash)) {
            Files.deleteIfExists(mergedFilePath);
            throw new IOException("文件合并后MD5验证失败");
        }
        
        // 更新文档信息
        document.setStoredFilename(datePath + File.separator + storedFilename);
        document.setContentType(contentType);
        document.setUploadStatus("completed");
        document.setFilename(filename);
        document = documentRepository.save(document);
        
        // 删除分片文件和目录
        String chunkDir = uploadDir + File.separator + CHUNK_DIR + File.separator + fileHash;
        Path chunkDirPath = Paths.get(chunkDir);
        if (Files.exists(chunkDirPath)) {
            Files.walk(chunkDirPath)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        }
        
        return document;
    }

    @Override
    public Map<String, Object> checkUploadStatus(String fileHash) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<Document> docOpt = documentRepository.findByFileHashAndUploadStatus(fileHash, "uploading");
        if (!docOpt.isPresent()) {
            result.put("exists", false);
            return result;
        }
        
        Document document = docOpt.get();
        List<DocumentChunk> uploadedChunks = documentChunkRepository
            .findByDocumentIdAndUploadStatusOrderByChunkIndex(document.getId(), "completed");
        List<Integer> chunkIndices = uploadedChunks.stream()
            .map(DocumentChunk::getChunkIndex)
            .collect(Collectors.toList());
        
        result.put("exists", true);
        result.put("documentId", document.getId());
        result.put("uploadedChunks", chunkIndices);
        result.put("totalChunks", document.getTotalChunks());
        result.put("progress", document.getTotalChunks() > 0 ? 
            (document.getUploadedChunks() * 100.0 / document.getTotalChunks()) : 0);
        
        return result;
    }

    @Override
    public void downloadDocumentStream(Long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Document document = getDocumentById(id);
        if (document == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 增加下载次数
        document.setDownloadCount(document.getDownloadCount() + 1);
        documentRepository.save(document);

        Path filePath = Paths.get(uploadDir, document.getStoredFilename());
        if (!Files.exists(filePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 设置响应头
        response.setContentType(document.getContentType());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + document.getFilename() + "\"");
        response.setContentLengthLong(document.getFileSize());
        
        // 支持Range请求（断点续传下载）
        String rangeHeader = request.getHeader("Range");
        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            // 解析Range请求
            String[] ranges = rangeHeader.substring(6).split("-");
            long start = Long.parseLong(ranges[0]);
            long end = ranges.length > 1 && !ranges[1].isEmpty() ? 
                Long.parseLong(ranges[1]) : document.getFileSize() - 1;
            
            long contentLength = end - start + 1;
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Range", 
                String.format("bytes %d-%d/%d", start, end, document.getFileSize()));
            response.setContentLengthLong(contentLength);
            
            // 使用NIO零拷贝技术进行流式传输
            try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ);
                 WritableByteChannel outChannel = Channels.newChannel(response.getOutputStream())) {
                fileChannel.transferTo(start, contentLength, outChannel);
            }
        } else {
            // 普通下载，使用流式传输
            try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ);
                 WritableByteChannel outChannel = Channels.newChannel(response.getOutputStream())) {
                fileChannel.transferTo(0, document.getFileSize(), outChannel);
            }
        }
    }
}