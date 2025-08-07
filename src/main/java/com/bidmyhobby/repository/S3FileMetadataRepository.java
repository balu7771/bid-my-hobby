package com.bidmyhobby.repository;

import com.bidmyhobby.entity.S3FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface S3FileMetadataRepository extends JpaRepository<S3FileMetadata, Long> {
    
    Optional<S3FileMetadata> findByS3Key(String s3Key);
    
    List<S3FileMetadata> findByBucketName(String bucketName);
    
    List<S3FileMetadata> findByUploadedBy(String uploadedBy);
    
    @Query("SELECT s FROM S3FileMetadata s WHERE s.contentType LIKE %:contentType%")
    List<S3FileMetadata> findByContentTypeContaining(String contentType);
}