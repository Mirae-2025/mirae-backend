package com.yourcode.mirae.usermetadata.controller;

import com.yourcode.mirae.usermetadata.client.AIServerClient;
import com.yourcode.mirae.usermetadata.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-speedrun")
@RequiredArgsConstructor
@Tag(name = "AI Speedrun", description = "AI server speedrun session management APIs")
public class AISpeedrunController {

    private final AIServerClient aiServerClient;

    @Operation(summary = "Create a new speedrun session (only at :00 or :30)")
    @PostMapping("/create")
    public ResponseEntity<SpeedrunSessionResponse> createSession(
            @Valid @RequestBody SpeedrunCreateRequest request) {

        SpeedrunSessionResponse response = aiServerClient.createSpeedrunSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get speedrun session status")
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<SpeedrunSessionResponse> getSession(
            @Parameter(description = "Session ID", required = true)
            @PathVariable String sessionId) {

        SpeedrunSessionResponse response = aiServerClient.getSpeedrunSession(sessionId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Submit a solved problem")
    @PostMapping("/submit")
    public ResponseEntity<SpeedrunSubmitResponse> submitProblem(
            @Valid @RequestBody SpeedrunSubmitRequest request) {

        SpeedrunSubmitResponse response = aiServerClient.submitSpeedrunProblem(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get speedrun final result")
    @GetMapping("/result/{sessionId}")
    public ResponseEntity<SpeedrunResultResponse> getResult(
            @Parameter(description = "Session ID", required = true)
            @PathVariable String sessionId) {

        SpeedrunResultResponse response = aiServerClient.getSpeedrunResult(sessionId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get speedrun leaderboard")
    @GetMapping("/leaderboard")
    public ResponseEntity<SpeedrunLeaderboardResponse> getLeaderboard(
            @Parameter(description = "Mode: 30min, 60min, 90min, 120min")
            @RequestParam(value = "mode", defaultValue = "30min") String mode,
            @Parameter(description = "Number of entries")
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {

        SpeedrunLeaderboardResponse response = aiServerClient.getSpeedrunLeaderboard(mode, limit);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Check if user has an active speedrun session")
    @GetMapping("/active/{userId}")
    public ResponseEntity<SpeedrunSessionResponse> getActiveSession(
            @Parameter(description = "User ID", required = true)
            @PathVariable String userId) {

        SpeedrunSessionResponse response = aiServerClient.getActiveSpeedrun(userId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
}
