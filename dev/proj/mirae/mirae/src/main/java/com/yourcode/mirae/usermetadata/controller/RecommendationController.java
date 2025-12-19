package com.yourcode.mirae.usermetadata.controller;

import com.yourcode.mirae.usermetadata.client.AIServerClient;
import com.yourcode.mirae.usermetadata.dto.BatchRecommendRequest;
import com.yourcode.mirae.usermetadata.dto.BatchRecommendResponse;
import com.yourcode.mirae.usermetadata.dto.RecommendResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
@Tag(name = "Recommendation", description = "AI-powered problem recommendation APIs")
public class RecommendationController {

    private final AIServerClient aiServerClient;

    @Operation(summary = "Get TF-IDF based recommendations (default)")
    @GetMapping
    public ResponseEntity<RecommendResponse> getRecommendations(
            @Parameter(description = "User ID", required = true)
            @RequestParam("user_id") String userId,
            @Parameter(description = "Number of recommendations")
            @RequestParam(value = "k", defaultValue = "5") Integer k,
            @Parameter(description = "Exclude already solved problems")
            @RequestParam(value = "exclude_solved", defaultValue = "true") Boolean excludeSolved,
            @Parameter(description = "Minimum difficulty filter")
            @RequestParam(value = "difficulty_min", required = false) String difficultyMin,
            @Parameter(description = "Maximum difficulty filter")
            @RequestParam(value = "difficulty_max", required = false) String difficultyMax) {

        RecommendResponse response = aiServerClient.getRecommendations(
                userId, k, excludeSolved, difficultyMin, difficultyMax);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get popularity-based recommendations")
    @GetMapping("/popularity")
    public ResponseEntity<RecommendResponse> getPopularityRecommendations(
            @RequestParam("user_id") String userId,
            @RequestParam(value = "k", defaultValue = "5") Integer k,
            @RequestParam(value = "exclude_solved", defaultValue = "true") Boolean excludeSolved,
            @RequestParam(value = "difficulty_min", required = false) String difficultyMin,
            @RequestParam(value = "difficulty_max", required = false) String difficultyMax) {

        RecommendResponse response = aiServerClient.getPopularityRecommendations(
                userId, k, excludeSolved, difficultyMin, difficultyMax);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get random recommendations (A/B test baseline)")
    @GetMapping("/random")
    public ResponseEntity<RecommendResponse> getRandomRecommendations(
            @RequestParam("user_id") String userId,
            @RequestParam(value = "k", defaultValue = "5") Integer k,
            @RequestParam(value = "exclude_solved", defaultValue = "true") Boolean excludeSolved,
            @RequestParam(value = "difficulty_min", required = false) String difficultyMin,
            @RequestParam(value = "difficulty_max", required = false) String difficultyMax) {

        RecommendResponse response = aiServerClient.getRandomRecommendations(
                userId, k, excludeSolved, difficultyMin, difficultyMax);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get hybrid recommendations (TF-IDF 70% + Popularity 30%)")
    @GetMapping("/hybrid")
    public ResponseEntity<RecommendResponse> getHybridRecommendations(
            @RequestParam("user_id") String userId,
            @RequestParam(value = "k", defaultValue = "5") Integer k,
            @RequestParam(value = "exclude_solved", defaultValue = "true") Boolean excludeSolved,
            @RequestParam(value = "difficulty_min", required = false) String difficultyMin,
            @RequestParam(value = "difficulty_max", required = false) String difficultyMax) {

        RecommendResponse response = aiServerClient.getHybridRecommendations(
                userId, k, excludeSolved, difficultyMin, difficultyMax);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get weakness-based recommendations")
    @GetMapping("/weakness")
    public ResponseEntity<RecommendResponse> getWeaknessRecommendations(
            @RequestParam("user_id") String userId,
            @RequestParam(value = "k", defaultValue = "5") Integer k,
            @RequestParam(value = "exclude_solved", defaultValue = "true") Boolean excludeSolved,
            @RequestParam(value = "difficulty_min", required = false) String difficultyMin,
            @RequestParam(value = "difficulty_max", required = false) String difficultyMax) {

        RecommendResponse response = aiServerClient.getWeaknessRecommendations(
                userId, k, excludeSolved, difficultyMin, difficultyMax);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get batch recommendations for multiple users")
    @PostMapping("/batch")
    public ResponseEntity<BatchRecommendResponse> getBatchRecommendations(
            @Parameter(description = "Strategy: tfidf, popularity, random, hybrid, weakness")
            @RequestParam(value = "strategy", defaultValue = "tfidf") String strategy,
            @Valid @RequestBody BatchRecommendRequest request) {

        BatchRecommendResponse response = aiServerClient.getBatchRecommendations(request, strategy);
        return ResponseEntity.ok(response);
    }
}
