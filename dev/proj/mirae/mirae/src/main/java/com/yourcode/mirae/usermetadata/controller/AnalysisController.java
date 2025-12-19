package com.yourcode.mirae.usermetadata.controller;

import com.yourcode.mirae.usermetadata.client.AIServerClient;
import com.yourcode.mirae.usermetadata.dto.WeaknessAnalysisResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Tag(name = "Analysis", description = "User weakness analysis APIs")
public class AnalysisController {

    private final AIServerClient aiServerClient;

    @Operation(summary = "Analyze user's weak tags and failure patterns")
    @GetMapping("/weakness")
    public ResponseEntity<WeaknessAnalysisResponse> analyzeWeakness(
            @Parameter(description = "User ID", required = true)
            @RequestParam("user_id") String userId) {

        WeaknessAnalysisResponse response = aiServerClient.analyzeWeakness(userId);
        return ResponseEntity.ok(response);
    }
}
