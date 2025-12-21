package com.example.documentmanager.service;

import com.example.documentmanager.entity.Document;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DocumentService {
    Document saveDocument(MultipartFile file) throws IOException;
    List<Document> getAllDocuments();
    Document getDocumentById(Long id);
    void deleteDocument(Long id);
    void downloadDocument(Long id, HttpServletResponse response) throws IOException;
    
    // 分片上传相关方法
    Map<String, Object> initChunkUpload(String filename, String fileHash, Long fileSize, Integer totalChunks) throws IOException;
    Map<String, Object> uploadChunk(String fileHash, Integer chunkIndex, MultipartFile chunk) throws IOException;
    Document mergeChunks(String fileHash, String filename, String contentType) throws IOException;
    Map<String, Object> checkUploadStatus(String fileHash);
    
    // 流式下载
    void downloadDocumentStream(Long id, HttpServletRequest request, HttpServletResponse response) throws IOException;
}