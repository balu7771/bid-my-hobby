package com.bidmyhobby.service;

import com.bidmyhobby.config.AppConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.*;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ModerationService {

    @Value("${openai.api.key}")
    private String openaiApiKey;


    //private final AppConfig appConfig;

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

   /* public ModerationService(@Qualifier("appConfig") AppConfig appConfig) {
        this.appConfig = appConfig;
    }*/

    public boolean moderateText(String text) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/moderations"))
                .header("Authorization", "Bearer " + openaiApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"input\": \"" + text + "\"}"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map result = mapper.readValue(response.body(), Map.class);
        return !(Boolean)((Map)((java.util.List)result.get("results")).get(0)).get("flagged");
    }
}