package com.bidmyhobby.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties
public class AppConfig {

    // OpenAI
    private String openaiApiKey;

    // Scality / S3
    private String scalityEndpoint;
    private String scalityAccessKey;
    private String scalitySecretKey;
    private String scalityBucketName;

    // Getters and setters
    public String getOpenaiApiKey() {
        return openaiApiKey;
    }

    public void setOpenaiApiKey(String openaiApiKey) {
        this.openaiApiKey = openaiApiKey;
    }

    public String getScalityEndpoint() {
        return scalityEndpoint;
    }

    public void setScalityEndpoint(String scalityEndpoint) {
        this.scalityEndpoint = scalityEndpoint;
    }

    public String getScalityAccessKey() {
        return scalityAccessKey;
    }

    public void setScalityAccessKey(String scalityAccessKey) {
        this.scalityAccessKey = scalityAccessKey;
    }

    public String getScalitySecretKey() {
        return scalitySecretKey;
    }

    public void setScalitySecretKey(String scalitySecretKey) {
        this.scalitySecretKey = scalitySecretKey;
    }

    public String getScalityBucketName() {
        return scalityBucketName;
    }

    public void setScalityBucketName(String scalityBucketName) {
        this.scalityBucketName = scalityBucketName;
    }
}
