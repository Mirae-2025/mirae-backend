package com.yourcode.mirae.speedrun.controller;

import com.yourcode.mirae.speedrun.dto.UserStatsResponse;
import com.yourcode.mirae.speedrun.entity.SpeedrunResult;
import com.yourcode.mirae.speedrun.repository.SpeedrunResultRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/speedrun")
@RequiredArgsConstructor
@Tag(name = "Speedrun History", description = "Speedrun history and statistics API")
public class SpeedrunHistoryController {

    private final SpeedrunResultRepository resultRepository;

    @Operation(summary = "Get my game history")
    @GetMapping("/history")
    public ResponseEntity<Page<SpeedrunResult>> getHistory(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SpeedrunResult> history = resultRepository.findByUserId(userId, pageRequest);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Get my statistics")
    @GetMapping("/stats")
    public ResponseEntity<UserStatsResponse> getStats(
            @RequestHeader("X-User-Id") Long userId) {
        Double avgScore = resultRepository.getAverageScoreByUserId(userId);
        Long totalSolved = resultRepository.getTotalSolvedByUserId(userId);
        Long gameCount = resultRepository.getGameCountByUserId(userId);

        UserStatsResponse response = UserStatsResponse.builder()
                .userId(userId)
                .averageScore(avgScore != null ? avgScore : 0.0)
                .totalProblemsSolved(totalSolved != null ? totalSolved.intValue() : 0)
                .totalGames(gameCount != null ? gameCount.intValue() : 0)
                .build();

        return ResponseEntity.ok(response);
    }
}
