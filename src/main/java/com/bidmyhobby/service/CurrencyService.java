package com.bidmyhobby.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class CurrencyService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Using exchangerate-api.com (free tier: 1500 requests/month)
    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/INR";
    
    public Map<String, Double> convertFromINR(double inrAmount) {
        Map<String, Double> conversions = new HashMap<>();
        conversions.put("INR", inrAmount);
        
        try {
            String response = restTemplate.getForObject(API_URL, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode rates = jsonNode.get("rates");
            
            if (rates != null) {
                double usdRate = rates.get("USD").asDouble();
                double gbpRate = rates.get("GBP").asDouble();
                
                conversions.put("USD", round(inrAmount * usdRate, 2));
                conversions.put("GBP", round(inrAmount * gbpRate, 2));
            }
        } catch (Exception e) {
            // Fallback to approximate rates if API fails
            conversions.put("USD", round(inrAmount * 0.012, 2)); // ~1 INR = 0.012 USD
            conversions.put("GBP", round(inrAmount * 0.010, 2)); // ~1 INR = 0.010 GBP
        }
        
        return conversions;
    }
    
    private double round(double value, int places) {
        return BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }
}