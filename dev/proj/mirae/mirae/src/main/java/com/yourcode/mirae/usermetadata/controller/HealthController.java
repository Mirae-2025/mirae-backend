package com.yourcode.mirae.usermetadata.controller;

import com.yourcode.mirae.usermetadata.client.AIServerClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Health", description = "AI server health check")
public class HealthController {

    private final AIServerClient aiServerClient;

    @Operation(summary = "Check AI server health status")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        boolean healthy = aiServerClient.isHealthy();
        Map<String, Object> response = Map.of(
                "ai_server_status", healthy ? "ok" : "unavailable",
                "backend_status", "ok"
        );

        if (healthy) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(503).body(response);
        }
    }
}
