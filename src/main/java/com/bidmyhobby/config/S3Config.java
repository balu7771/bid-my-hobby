package com.bidmyhobby.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;

@Configuration
public class S3Config {

    @Value("${aws.s3.bucket.name}")
    private String bucketName;
    
    @Value("${aws.s3.bucket.metadata-prefix}")
    private String metadataPrefix;
    
    @Value("${aws.s3.bucket.images-prefix}")
    private String imagesPrefix;
    
    @Value("${aws.s3.bucket.users-prefix}")
    private String usersPrefix;
    
    public String getBucketName() {
        return bucketName;
    }
    
    public String getMetadataPrefix() {
        return metadataPrefix;
    }
    
    public String getImagesPrefix() {
        return imagesPrefix;
    }
    
    public String getUsersPrefix() {
        return usersPrefix;
    }
}