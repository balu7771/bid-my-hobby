package com.bidmyhobby.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private HealthEndpoint healthEndpoint;

    @GetMapping
    public ResponseEntity<?> health() {
        HealthComponent health = healthEndpoint.health();
        return ResponseEntity.ok().body(health);
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("version", "1.0.0");
        status.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok().body(status);
    }
}