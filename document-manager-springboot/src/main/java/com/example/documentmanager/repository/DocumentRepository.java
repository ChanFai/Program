package com.example.documentmanager.repository;

import com.example.documentmanager.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByFileHash(String fileHash);
    List<Document> findByFilename(String filename);
    Optional<Document> findByFilenameAndFileHash(String filename, String fileHash);
    Optional<Document> findByFileHashAndUploadStatus(String fileHash, String uploadStatus);
    List<Document> findByUploadStatus(String uploadStatus);
}