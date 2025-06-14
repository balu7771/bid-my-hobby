package com.bidmyhobby.service;

import com.bidmyhobby.config.AppConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ModerationService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Moderates text content using OpenAI's moderation API
     * @param text The text to moderate
     * @return true if the content is appropriate, false if flagged
     */
    public boolean moderateText(String text) throws Exception {
        try {
            // Handle empty or null text
            if (text == null || text.trim().isEmpty()) {
                return true; // Consider empty text as appropriate
            }
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/moderations"))
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of("input", text))))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Map result = mapper.readValue(response.body(), Map.class);
            
            // Check if response contains results
            if (result == null || !result.containsKey("results") || result.get("results") == null) {
                System.err.println("Invalid moderation API response: " + response.body());
                return true; // Default to allowing content if API response is invalid
            }
            
            List<Map<String, Object>> results = (List<Map<String, Object>>) result.get("results");
            if (results.isEmpty()) {
                return true; // No results means no flags
            }
            
            Map<String, Object> firstResult = results.get(0);
            if (firstResult == null || !firstResult.containsKey("flagged")) {
                return true; // No flagged field means we can't determine, so allow
            }
            
            return !(Boolean) firstResult.get("flagged");
        } catch (Exception e) {
            System.err.println("Error in content moderation: " + e.getMessage());
            return true; // Default to allowing content if there's an error
        }
    }
    
    /**
     * Analyzes an image using OpenAI's Vision API (GPT-4 Vision)
     * @param file The image file to analyze
     * @return A description of the image content
     */
    public String analyzeImage(MultipartFile file) throws Exception {
        try {
            // Convert image to base64
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            
            // Create request payload
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o");
            
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            
            Map<String, Object> contentItem = new HashMap<>();
            contentItem.put("type", "text");
            contentItem.put("text", "Describe this image in detail. What is it? Is it a hobby item?");
            
            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image_url");
            
            Map<String, String> imageUrl = new HashMap<>();
            imageUrl.put("url", "data:image/" + getImageFormat(file) + ";base64," + base64Image);
            
            imageContent.put("image_url", imageUrl);
            
            List<Map<String, Object>> contentList = List.of(contentItem, imageContent);
            message.put("content", contentList);
            
            requestBody.put("messages", List.of(message));
            requestBody.put("max_tokens", 300);
            
            // Send request to OpenAI API
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Map result = mapper.readValue(response.body(), Map.class);
            
            // Check for error in the response
            if (result.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) result.get("error");
                throw new RuntimeException("OpenAI API error: " + error.get("message"));
            }
            
            // Extract description from response
            List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("Invalid response from OpenAI API: No choices returned");
            }
            
            Map<String, Object> choice = choices.get(0);
            Map<String, Object> responseMessage = (Map<String, Object>) choice.get("message");
            if (responseMessage == null) {
                throw new RuntimeException("Invalid response from OpenAI API: No message in choice");
            }
            
            String responseContent = (String) responseMessage.get("content");
            if (responseContent == null) {
                throw new RuntimeException("Invalid response from OpenAI API: No content in message");
            }
            
            return responseContent;
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error analyzing image: " + e.getMessage());
            
            // Return a default description to allow the upload to continue
            return "This appears to be an image of a hobby item.";
        }
    }
    
    /**
     * Determines if the image description indicates a hobby item
     * @param description The image description from Vision API
     * @return true if it appears to be a hobby item
     */
    public boolean isHobbyItem(String description) throws Exception {
        try {
            // Create request payload for classification
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o");
            
            String prompt = "Based on this description, determine if this is a hobby item. " +
                            "A hobby item is something used for recreational activities, collections, crafts, " +
                            "or creative pursuits. It should NOT be a person, animal, inappropriate content, " +
                            "or something unrelated to hobbies. Respond with ONLY 'true' if it is a hobby item, " +
                            "or 'false' if it is not.\n\nDescription: " + description;
            
            requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are a classifier that responds with only 'true' or 'false'."),
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("max_tokens", 10);
            
            // Send request to OpenAI API
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Map result = mapper.readValue(response.body(), Map.class);
            
            // Check for error in the response
            if (result.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) result.get("error");
                System.err.println("OpenAI API error: " + error.get("message"));
                return true; // Default to allowing upload if API fails
            }
            
            // Extract classification from response
            List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
            if (choices == null || choices.isEmpty()) {
                System.err.println("Invalid response from OpenAI API: No choices returned");
                return true; // Default to allowing upload if response is invalid
            }
            
            Map<String, Object> choice = choices.get(0);
            if (choice == null || !choice.containsKey("message")) {
                System.err.println("Invalid choice structure in API response");
                return true;
            }
            
            Map<String, Object> responseMessage = (Map<String, Object>) choice.get("message");
            if (responseMessage == null || !responseMessage.containsKey("content")) {
                System.err.println("Invalid message structure in API response");
                return true;
            }
            
            String responseContent = ((String) responseMessage.get("content")).trim().toLowerCase();
            return responseContent.contains("true");
        } catch (Exception e) {
            System.err.println("Error checking if item is a hobby item: " + e.getMessage());
            return true; // Default to allowing upload if there's an error
        }
    }
    
    private String getImageFormat(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            return contentType.substring(contentType.indexOf('/') + 1);
        }
        return "jpeg"; // Default format
    }
}