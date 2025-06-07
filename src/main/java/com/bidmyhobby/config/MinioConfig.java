package com.bidmyhobby.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${scality.access.key}")
    private String accessKey;

    @Value("${scality.secret.key}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint("https://s3.amazonaws.com")
                .credentials(accessKey, secretKey)
                .build();
    }
}