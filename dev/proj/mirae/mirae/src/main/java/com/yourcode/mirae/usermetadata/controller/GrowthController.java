package com.yourcode.mirae.usermetadata.controller;

import com.yourcode.mirae.usermetadata.client.AIServerClient;
import com.yourcode.mirae.usermetadata.dto.AccuracyTrendResponse;
import com.yourcode.mirae.usermetadata.dto.GrowthReportResponse;
import com.yourcode.mirae.usermetadata.dto.TagAnalysisResponse;
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
@RequestMapping("/api/growth")
@RequiredArgsConstructor
@Tag(name = "Growth", description = "User growth analysis APIs")
public class GrowthController {

    private final AIServerClient aiServerClient;

    @Operation(summary = "Get comprehensive growth report")
    @GetMapping("/report")
    public ResponseEntity<GrowthReportResponse> getGrowthReport(
            @Parameter(description = "User ID", required = true)
            @RequestParam("user_id") String userId,
            @Parameter(description = "Analysis period in days (7-365)")
            @RequestParam(value = "days", defaultValue = "30") Integer days) {

        GrowthReportResponse response = aiServerClient.getGrowthReport(userId, days);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get weekly accuracy trend")
    @GetMapping("/accuracy-trend")
    public ResponseEntity<AccuracyTrendResponse> getAccuracyTrend(
            @Parameter(description = "User ID", required = true)
            @RequestParam("user_id") String userId,
            @Parameter(description = "Analysis period in days (7-365)")
            @RequestParam(value = "days", defaultValue = "30") Integer days) {

        AccuracyTrendResponse response = aiServerClient.getAccuracyTrend(userId, days);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get tag-based analysis")
    @GetMapping("/tags")
    public ResponseEntity<TagAnalysisResponse> getTagAnalysis(
            @Parameter(description = "User ID", required = true)
            @RequestParam("user_id") String userId,
            @Parameter(description = "Analysis period in days (7-365)")
            @RequestParam(value = "days", defaultValue = "30") Integer days) {

        TagAnalysisResponse response = aiServerClient.getTagAnalysis(userId, days);
        return ResponseEntity.ok(response);
    }
}
