package com.msel.app.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        log.info("GET /health - Health check");
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "bankingOps");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> ready() {
        log.info("GET /ready - Readiness check");
        Map<String, String> response = new HashMap<>();
        response.put("ready", "true");
        response.put("service", "bankingOps");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        log.info("GET /metrics - Metrics endpoint");
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("timestamp", System.currentTimeMillis());
        metrics.put("service", "bankingOps");
        metrics.put("version", "0.0.1");
        return ResponseEntity.ok(metrics);
    }
}
