package com.yourcode.mirae.speedrun.service;

import com.yourcode.mirae.speedrun.redis.ClaimStatus;
import com.yourcode.mirae.speedrun.redis.RedisKeyUtil;
import com.yourcode.mirae.speedrun.verification.SubmitVerificationService;
import com.yourcode.mirae.speedrun.verification.VerificationResult;
import com.yourcode.mirae.speedrun.verification.VerificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionVerificationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SubmitVerificationService verificationService;
    private final RankingService rankingService;
    private final PenaltyService penaltyService;
    private final ScoreCalculationService scoreService;

    @Async
    public void verifySession(String sessionId) {
        log.info("Starting verification for session: {}", sessionId);

        String sessionKey = RedisKeyUtil.sessionKey(sessionId);
        String startAtStr = (String) redisTemplate.opsForHash().get(sessionKey, "start_at");

        if (startAtStr == null) {
            log.warn("Session start time not found for session: {}", sessionId);
            return;
        }

        LocalDateTime sessionStart = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(Long.parseLong(startAtStr)),
                ZoneId.of("Asia/Seoul")
        );

        String playersKey = RedisKeyUtil.sessionPlayersKey(sessionId);
        Set<String> players = redisTemplate.opsForSet().members(playersKey);

        if (players == null || players.isEmpty()) {
            log.info("No players in session: {}", sessionId);
            return;
        }

        for (String userIdStr : players) {
            Long userId = Long.parseLong(userIdStr);
            verifyUserClaims(sessionId, userId, sessionStart);
        }

        log.info("Verification completed for session: {}", sessionId);
    }

    private void verifyUserClaims(String sessionId, Long userId, LocalDateTime sessionStart) {
        String claimsKey = RedisKeyUtil.sessionClaimsKey(sessionId, userId);
        Map<Object, Object> claims = redisTemplate.opsForHash().entries(claimsKey);

        if (claims.isEmpty()) {
            return;
        }

        for (Map.Entry<Object, Object> entry : claims.entrySet()) {
            String problemIdStr = (String) entry.getKey();
            String status = (String) entry.getValue();

            if (!ClaimStatus.PENDING.name().equals(status)) {
                continue;
            }

            Long problemId = Long.parseLong(problemIdStr);
            VerificationResult result = verificationService.verify(userId, problemId, sessionStart);

            switch (result.getStatus()) {
                case VERIFIED:
                    redisTemplate.opsForHash().put(claimsKey, problemIdStr, ClaimStatus.VERIFIED.name());
                    log.debug("Claim verified: userId={}, problemId={}", userId, problemId);
                    break;

                case REJECTED:
                    redisTemplate.opsForHash().put(claimsKey, problemIdStr, ClaimStatus.REJECTED.name());

                    // Deduct score
                    int deductScore = scoreService.getBaseScore(0); // Use base score for unrated
                    rankingService.deductScore(sessionId, userId, deductScore);

                    // Add penalty
                    penaltyService.addFalseClaimPenalty(userId);
                    log.warn("Claim rejected: userId={}, problemId={}, reason={}", userId, problemId, result.getSource());
                    break;

                case PENDING:
                    log.debug("Claim still pending: userId={}, problemId={}", userId, problemId);
                    break;
            }
        }
    }
}
