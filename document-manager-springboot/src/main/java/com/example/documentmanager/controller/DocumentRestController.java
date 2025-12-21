package com.example.documentmanager.controller;

import com.example.documentmanager.entity.Document;
import com.example.documentmanager.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*") // 允许跨域请求
public class DocumentRestController {

    @Autowired
    private DocumentService documentService;

    /**
     * 获取所有文档列表
     */
    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        List<Document> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    /**
     * 根据ID获取文档信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        Document document = documentService.getDocumentById(id);
        if (document != null) {
            return ResponseEntity.ok(document);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 上传文件
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadDocument(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        if (file.isEmpty()) {
            response.put("success", false);
            response.put("message", "请选择一个文件上传");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Document document = documentService.saveDocument(file);
            response.put("success", true);
            response.put("message", "文件上传成功");
            response.put("document", document);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 下载文件（普通下载）
     */
    @GetMapping("/{id}/download")
    public void downloadDocument(@PathVariable Long id, HttpServletResponse response) throws IOException {
        documentService.downloadDocument(id, response);
    }

    /**
     * 流式下载文件（支持断点续传）
     */
    @GetMapping("/{id}/download-stream")
    public void downloadDocumentStream(@PathVariable Long id, 
                                       HttpServletRequest request,
                                       HttpServletResponse response) throws IOException {
        documentService.downloadDocumentStream(id, request, response);
    }

    /**
     * 初始化分片上传
     */
    @PostMapping("/chunk/init")
    public ResponseEntity<Map<String, Object>> initChunkUpload(
            @RequestParam("filename") String filename,
            @RequestParam("fileHash") String fileHash,
            @RequestParam("fileSize") Long fileSize,
            @RequestParam("totalChunks") Integer totalChunks) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> result = documentService.initChunkUpload(filename, fileHash, fileSize, totalChunks);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // 捕获所有异常，包括数据库异常
            e.printStackTrace(); // 打印堆栈跟踪以便调试
            response.put("success", false);
            response.put("message", "初始化上传失败: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 上传分片
     */
    @PostMapping("/chunk/upload")
    public ResponseEntity<Map<String, Object>> uploadChunk(
            @RequestParam("fileHash") String fileHash,
            @RequestParam("chunkIndex") Integer chunkIndex,
            @RequestParam("chunk") MultipartFile chunk) {
        Map<String, Object> response = new HashMap<>();
        
        if (chunk.isEmpty()) {
            response.put("success", false);
            response.put("message", "分片文件为空");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Map<String, Object> result = documentService.uploadChunk(fileHash, chunkIndex, chunk);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "分片上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 合并分片
     */
    @PostMapping("/chunk/merge")
    public ResponseEntity<Map<String, Object>> mergeChunks(
            @RequestParam("fileHash") String fileHash,
            @RequestParam("filename") String filename,
            @RequestParam("contentType") String contentType) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Document document = documentService.mergeChunks(fileHash, filename, contentType);
            response.put("success", true);
            response.put("message", "文件上传成功");
            response.put("document", document);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "文件合并失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 检查上传状态（用于断点续传）
     */
    @GetMapping("/chunk/status")
    public ResponseEntity<Map<String, Object>> checkUploadStatus(@RequestParam("fileHash") String fileHash) {
        Map<String, Object> result = documentService.checkUploadStatus(fileHash);
        return ResponseEntity.ok(result);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteDocument(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            documentService.deleteDocument(id);
            response.put("success", true);
            response.put("message", "文件删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "文件删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}