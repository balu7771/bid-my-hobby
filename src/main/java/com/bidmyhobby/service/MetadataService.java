package com.bidmyhobby.service;

import com.bidmyhobby.entity.S3FileMetadata;
import com.bidmyhobby.repository.S3FileMetadataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class MetadataService {

    @Autowired
    private S3FileMetadataRepository s3FileMetadataRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void saveFileMetadata(String s3Key, String bucketName, String fileName, 
                                String contentType, Long fileSize, Map<String, Object> metadata, 
                                String uploadedBy) {
        S3FileMetadata fileMetadata = new S3FileMetadata(s3Key, bucketName, fileName);
        fileMetadata.setContentType(contentType);
        fileMetadata.setFileSize(fileSize);
        fileMetadata.setMetadata(metadata);
        fileMetadata.setUploadedBy(uploadedBy);
        fileMetadata.setUploadedAt(LocalDateTime.now());
        
        s3FileMetadataRepository.save(fileMetadata);
    }
}