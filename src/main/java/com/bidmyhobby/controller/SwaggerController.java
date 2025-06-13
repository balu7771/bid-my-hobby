package com.bidmyhobby.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/api/docs")
public class SwaggerController {

    @GetMapping("/download")
    public ResponseEntity<String> downloadSwaggerJson() {
        try {
            // Fetch the Swagger JSON from the local OpenAPI endpoint
            RestTemplate restTemplate = new RestTemplate();
            String swaggerJson = restTemplate.getForObject("http://localhost:8080/v3/api-docs", String.class);
            
            // Set headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", "bid-my-hobby-api.json");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(swaggerJson);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error downloading Swagger documentation: " + e.getMessage());
        }
    }
}