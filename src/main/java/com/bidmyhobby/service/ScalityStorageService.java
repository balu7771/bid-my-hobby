package com.bidmyhobby.service;

import io.minio.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ScalityStorageService {

    private final MinioClient minioClient;
    
    @Value("${scality.bucket.name}")
    private String bucket;
    
    @Value("${scality.endpoint}")
    private String endpoint;

    public ScalityStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String uploadFile(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        String filename = System.currentTimeMillis() + "-" + file.getOriginalFilename();

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(filename)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        return endpoint + "/" + bucket + "/" + filename;
    }
    
    public List<Map<String, String>> listAllItems() throws Exception {
        List<Map<String, String>> items = new ArrayList<>();
        
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .build()
        );
        
        for (Result<Item> result : results) {
            Item item = result.get();
            if (!item.isDir()) {
                Map<String, String> itemInfo = new HashMap<>();
                itemInfo.put("name", item.objectName());
                itemInfo.put("size", String.valueOf(item.size()));
                itemInfo.put("lastModified", item.lastModified().toString());
                itemInfo.put("url", endpoint + "/" + bucket + "/" + item.objectName());
                items.add(itemInfo);
            }
        }
        
        return items;
    }
}