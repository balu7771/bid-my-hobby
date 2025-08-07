package com.bidmyhobby.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.*;
import java.util.Base64;

@Service
public class AIService {
    
    @Value("${openai.api.key:}")
    private String openaiApiKey;
    
    private final Random random = new Random();
    
    // Categories for hobby items
    private final String[] categories = {
        "Arts & Crafts", "Photography", "Woodworking", "Jewelry", "Pottery", 
        "Textiles", "Painting", "Sculpture", "Digital Art", "Collectibles",
        "Model Making", "Electronics", "Gardening", "Cooking", "Music"
    };
    
    public Map<String, Object> analyzeItemForPriceAndCategory(MultipartFile file, String description, String name) {
        Map<String, Object> analysis = new HashMap<>();
        
        try {
            // Try OpenAI Vision API first
            if (openaiApiKey != null && !openaiApiKey.isEmpty()) {
                Map<String, Object> aiAnalysis = getOpenAIPriceEstimate(file, name, description);
                if (aiAnalysis != null) {
                    return aiAnalysis;
                }
            }
            
            // Fallback to rule-based system
            String category = determineCategory(name, description);
            BigDecimal estimatedPrice = estimatePrice(name, description, category);
            
            analysis.put("category", category);
            analysis.put("estimatedPrice", estimatedPrice.doubleValue());
            analysis.put("confidence", 0.6);
            
        } catch (Exception e) {
            analysis.put("category", "Arts & Crafts");
            analysis.put("estimatedPrice", 400.0);
            analysis.put("confidence", 0.5);
        }
        
        return analysis;
    }
    
    private String determineCategory(String name, String description) {
        String text = (name + " " + description).toLowerCase();
        
        // Check for drawing/sketching first
        if (text.contains("sketch") || text.contains("pencil") || text.contains("drawing") || 
            text.contains("doodle") || text.contains("charcoal")) {
            return "Arts & Crafts";
        } else if (text.contains("photo") || text.contains("camera") || text.contains("picture")) {
            return "Photography";
        } else if (text.contains("wood") || text.contains("carv") || text.contains("furniture")) {
            return "Woodworking";
        } else if (text.contains("paint") || text.contains("canvas") || text.contains("brush") || text.contains("acrylic")) {
            return "Painting";
        } else if (text.contains("jewelry") || text.contains("ring") || text.contains("necklace") || text.contains("bracelet")) {
            return "Jewelry";
        } else if (text.contains("clay") || text.contains("ceramic") || text.contains("pottery")) {
            return "Pottery";
        } else if (text.contains("fabric") || text.contains("sew") || text.contains("knit") || text.contains("crochet")) {
            return "Textiles";
        } else if (text.contains("digital") || text.contains("computer") || text.contains("graphic")) {
            return "Digital Art";
        } else if (text.contains("model") || text.contains("miniature") || text.contains("scale")) {
            return "Model Making";
        } else if (text.contains("electronic") || text.contains("circuit") || text.contains("arduino")) {
            return "Electronics";
        } else if (text.contains("garden") || text.contains("plant") || text.contains("flower")) {
            return "Gardening";
        } else if (text.contains("cook") || text.contains("bake") || text.contains("recipe")) {
            return "Cooking";
        } else if (text.contains("music") || text.contains("instrument") || text.contains("song")) {
            return "Music";
        } else if (text.contains("collect") || text.contains("vintage") || text.contains("antique")) {
            return "Collectibles";
        } else if (text.contains("sculpt") || text.contains("statue") || text.contains("bronze")) {
            return "Sculpture";
        } else {
            return "Arts & Crafts";
        }
    }
    
    private BigDecimal estimatePrice(String name, String description, String category) {
        String text = (name + " " + description).toLowerCase();
        
        // More realistic base prices by category (in INR)
        BigDecimal basePrice = switch (category) {
            case "Photography" -> new BigDecimal("800");
            case "Woodworking" -> new BigDecimal("1200");
            case "Jewelry" -> new BigDecimal("1500");
            case "Pottery" -> new BigDecimal("600");
            case "Painting" -> new BigDecimal("400");
            case "Textiles" -> new BigDecimal("500");
            case "Digital Art" -> new BigDecimal("300");
            case "Sculpture" -> new BigDecimal("1000");
            case "Collectibles" -> new BigDecimal("700");
            case "Electronics" -> new BigDecimal("1800");
            default -> new BigDecimal("400");
        };
        
        // Check for simple/basic items first
        if (text.contains("sketch") || text.contains("pencil") || text.contains("drawing") || 
            text.contains("simple") || text.contains("basic") || text.contains("test")) {
            basePrice = new BigDecimal("50");
        }
        
        // Adjust based on keywords
        if (text.contains("handmade") || text.contains("custom")) {
            basePrice = basePrice.multiply(new BigDecimal("1.2"));
        }
        if (text.contains("vintage") || text.contains("antique")) {
            basePrice = basePrice.multiply(new BigDecimal("1.3"));
        }
        if (text.contains("large") || text.contains("big")) {
            basePrice = basePrice.multiply(new BigDecimal("1.1"));
        }
        if (text.contains("small") || text.contains("mini") || text.contains("tiny")) {
            basePrice = basePrice.multiply(new BigDecimal("0.7"));
        }
        if (text.contains("professional") || text.contains("premium")) {
            basePrice = basePrice.multiply(new BigDecimal("1.3"));
        }
        
        // Add some randomness (±15%)
        double multiplier = 0.85 + (random.nextDouble() * 0.3);
        basePrice = basePrice.multiply(new BigDecimal(multiplier));
        
        // Round to nearest 50 INR for smaller amounts, 100 INR for larger
        int price = basePrice.intValue();
        int rounded;
        if (price < 500) {
            rounded = ((price + 25) / 50) * 50;
        } else {
            rounded = ((price + 50) / 100) * 100;
        }
        return new BigDecimal(Math.max(50, rounded));
    }
    
    private Map<String, Object> getOpenAIPriceEstimate(MultipartFile file, String name, String description) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            String imageUrl = "data:" + file.getContentType() + ";base64," + base64Image;
            
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);
            
            String prompt = String.format(
                "Analyze this hobby item image. Item name: '%s'. Description: '%s'. " +
                "Provide a realistic market price estimate in Indian Rupees (INR) for this handmade/hobby item. " +
                "Consider: material quality, craftsmanship, size, complexity, and Indian market rates. " +
                "Respond with JSON: {\"price\": number, \"category\": \"string\", \"reasoning\": \"string\"}",
                name, description
            );
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o");
            requestBody.put("max_tokens", 300);
            
            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            
            List<Map<String, Object>> content = new ArrayList<>();
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", prompt);
            content.add(textContent);
            
            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image_url");
            Map<String, String> imageUrlMap = new HashMap<>();
            imageUrlMap.put("url", imageUrl);
            imageContent.put("image_url", imageUrlMap);
            content.add(imageContent);
            
            message.put("content", content);
            messages.add(message);
            requestBody.put("messages", messages);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions", entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                String aiResponse = root.path("choices").get(0).path("message").path("content").asText();
                
                // Clean the response - remove markdown formatting if present
                String cleanedResponse = aiResponse.trim();
                if (cleanedResponse.startsWith("```json")) {
                    cleanedResponse = cleanedResponse.substring(7);
                }
                if (cleanedResponse.startsWith("```")) {
                    cleanedResponse = cleanedResponse.substring(3);
                }
                if (cleanedResponse.endsWith("```")) {
                    cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
                }
                cleanedResponse = cleanedResponse.trim();
                
                // Parse JSON response
                JsonNode aiJson = mapper.readTree(cleanedResponse);
                double price = aiJson.path("price").asDouble(400);
                String category = aiJson.path("category").asText("Arts & Crafts");
                
                Map<String, Object> result = new HashMap<>();
                result.put("estimatedPrice", Math.max(50, Math.min(10000, price)));
                result.put("category", category);
                result.put("confidence", 0.9);
                return result;
            }
        } catch (Exception e) {
            System.err.println("OpenAI Vision API error: " + e.getMessage());
        }
        return null;
    }
    
    public Map<String, Object> validatePriceAndEstimate(BigDecimal userPrice, String name, String description) {
        Map<String, Object> result = new HashMap<>();
        
        // Use fallback estimation for validation
        String category = determineCategory(name, description);
        BigDecimal aiEstimate = estimatePrice(name, description, category);
        
        // Very lenient validation - allow user pricing flexibility
        BigDecimal minAllowedPrice = aiEstimate.multiply(new BigDecimal("0.1"));
        boolean isValid = userPrice.compareTo(minAllowedPrice) >= 0;
        
        result.put("isValid", isValid);
        result.put("aiEstimate", aiEstimate.doubleValue());
        result.put("userPrice", userPrice.doubleValue());
        result.put("category", category);
        
        if (!isValid) {
            result.put("reason", String.format(
                "Your base price of ₹%.0f seems too low. Please set at least ₹%.0f.",
                userPrice.doubleValue(), minAllowedPrice.doubleValue()
            ));
        }
        
        return result;
    }
}