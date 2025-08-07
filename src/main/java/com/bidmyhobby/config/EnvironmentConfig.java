package com.bidmyhobby.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Configuration
public class EnvironmentConfig {

    @PostConstruct
    public void loadEnvironmentVariables() {
        try {
            Path envPath = Paths.get(".env");
            if (Files.exists(envPath)) {
                Properties props = new Properties();
                props.load(new FileInputStream(envPath.toFile()));
                
                // Set system properties for environment variables that aren't already set
                props.forEach((key, value) -> {
                    String keyStr = key.toString();
                    if (System.getenv(keyStr) == null && System.getProperty(keyStr) == null) {
                        System.setProperty(keyStr, value.toString());
                    }
                });
                
                System.out.println("Loaded .env file successfully");
            } else {
                System.out.println(".env file not found in current directory");
            }
        } catch (IOException e) {
            System.err.println("Error loading .env file: " + e.getMessage());
        }
    }
}