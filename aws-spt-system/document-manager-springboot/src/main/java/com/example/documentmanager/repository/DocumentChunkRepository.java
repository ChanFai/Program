package com.example.documentmanager.repository;

import com.example.documentmanager.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    List<DocumentChunk> findByDocumentIdOrderByChunkIndex(Long documentId);
    Optional<DocumentChunk> findByDocumentIdAndChunkIndex(Long documentId, Integer chunkIndex);
    void deleteByDocumentId(Long documentId);
    List<DocumentChunk> findByDocumentIdAndUploadStatusOrderByChunkIndex(Long documentId, String uploadStatus);
}