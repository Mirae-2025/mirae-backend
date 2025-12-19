package com.yourcode.mirae.speedrun.controller;

import com.yourcode.mirae.speedrun.dto.*;
import com.yourcode.mirae.speedrun.redis.GameMode;
import com.yourcode.mirae.speedrun.service.RankingService;
import com.yourcode.mirae.speedrun.service.SessionService;
import com.yourcode.mirae.speedrun.websocket.LeaderboardBroadcastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/speedrun")
@RequiredArgsConstructor
@Tag(name = "Speedrun", description = "Speedrun minigame API")
public class SpeedrunController {

    private final SessionService sessionService;
    private final RankingService rankingService;
    private final LeaderboardBroadcastService broadcastService;

    // ==================== Session Management ====================

    @Operation(summary = "Create Classic mode session")
    @PostMapping("/classic/session")
    public ResponseEntity<SessionResponse> createClassicSession(
            @Valid @RequestBody CreateSessionRequest request,
            @Parameter(description = "User ID", required = true) @RequestHeader("X-User-Id") Long userId) {
        SessionResponse response = sessionService.createSession(GameMode.CLASSIC, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Create TagFocus mode session")
    @PostMapping("/tagfocus/session")
    public ResponseEntity<SessionResponse> createTagFocusSession(
            @Valid @RequestBody CreateSessionRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        SessionResponse response = sessionService.createSession(GameMode.TAGFOCUS, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Create Retry mode session")
    @PostMapping("/retry/session")
    public ResponseEntity<SessionResponse> createRetrySession(
            @Valid @RequestBody CreateSessionRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        SessionResponse response = sessionService.createSession(GameMode.RETRY, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get session info")
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<SessionResponse> getSession(
            @PathVariable String sessionId) {
        SessionResponse response = sessionService.getSession(sessionId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get session state")
    @GetMapping("/session/{sessionId}/state")
    public ResponseEntity<SessionStateResponse> getSessionState(
            @PathVariable String sessionId,
            @RequestHeader("X-User-Id") Long userId) {
        SessionStateResponse response = sessionService.getSessionState(sessionId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Start session (owner only)")
    @PostMapping("/session/{sessionId}/start")
    public ResponseEntity<SessionResponse> startSession(
            @PathVariable String sessionId,
            @RequestHeader("X-User-Id") Long userId) {
        SessionResponse response = sessionService.startSession(sessionId, userId);
        broadcastService.broadcastSessionState(sessionId, "RUNNING");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Join session")
    @PostMapping("/session/{sessionId}/join")
    public ResponseEntity<SessionResponse> joinSession(
            @PathVariable String sessionId,
            @RequestHeader("X-User-Id") Long userId) {
        SessionResponse response = sessionService.joinSession(sessionId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Leave session")
    @PostMapping("/session/{sessionId}/leave")
    public ResponseEntity<SessionResponse> leaveSession(
            @PathVariable String sessionId,
            @RequestHeader("X-User-Id") Long userId) {
        SessionResponse response = sessionService.leaveSession(sessionId, userId);
        return ResponseEntity.ok(response);
    }

    // ==================== Game Play ====================

    @Operation(summary = "Submit problem solution")
    @PostMapping("/session/{sessionId}/submit")
    public ResponseEntity<SubmitResponse> submitProblem(
            @PathVariable String sessionId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody SubmitRequest request) {
        SubmitResponse response = sessionService.submitProblem(sessionId, userId, request);
        broadcastService.broadcastLeaderboard(sessionId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get my status in session")
    @GetMapping("/session/{sessionId}/my-status")
    public ResponseEntity<UserStatusResponse> getMyStatus(
            @PathVariable String sessionId,
            @RequestHeader("X-User-Id") Long userId) {
        UserStatusResponse response = sessionService.getUserStatus(sessionId, userId);
        return ResponseEntity.ok(response);
    }

    // ==================== Rankings ====================

    @Operation(summary = "Get session leaderboard")
    @GetMapping("/session/{sessionId}/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> getSessionLeaderboard(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "10") int limit) {
        List<LeaderboardEntry> leaderboard = rankingService.getSessionLeaderboard(sessionId, limit);
        return ResponseEntity.ok(leaderboard);
    }

    @Operation(summary = "Get global ranking")
    @GetMapping("/ranking/global")
    public ResponseEntity<RankingResponse> getGlobalRanking(
            @RequestParam(defaultValue = "100") int limit) {
        RankingResponse response = rankingService.getGlobalRanking(limit);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get weekly ranking")
    @GetMapping("/ranking/weekly")
    public ResponseEntity<RankingResponse> getWeeklyRanking(
            @RequestParam(defaultValue = "100") int limit) {
        RankingResponse response = rankingService.getWeeklyRanking(limit);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get monthly ranking")
    @GetMapping("/ranking/monthly")
    public ResponseEntity<RankingResponse> getMonthlyRanking(
            @RequestParam(defaultValue = "100") int limit) {
        RankingResponse response = rankingService.getMonthlyRanking(limit);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get my ranking")
    @GetMapping("/ranking/my")
    public ResponseEntity<LeaderboardEntry> getMyRanking(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "global") String type) {
        LeaderboardEntry ranking = rankingService.getMyRanking(userId, type);
        return ResponseEntity.ok(ranking);
    }
}
