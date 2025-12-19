package com.yourcode.mirae.speedrun.service;

import com.yourcode.mirae.speedrun.dto.LeaderboardEntry;
import com.yourcode.mirae.speedrun.dto.RankingResponse;
import com.yourcode.mirae.speedrun.redis.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {

    private final RedisTemplate<String, String> redisTemplate;

    public List<LeaderboardEntry> getSessionLeaderboard(String sessionId, int limit) {
        String lbKey = RedisKeyUtil.sessionLeaderboardKey(sessionId);
        Set<ZSetOperations.TypedTuple<String>> rankings = redisTemplate.opsForZSet()
                .reverseRangeWithScores(lbKey, 0, limit - 1);

        if (rankings == null || rankings.isEmpty()) {
            return Collections.emptyList();
        }

        List<LeaderboardEntry> entries = new ArrayList<>();
        int rank = 1;

        for (ZSetOperations.TypedTuple<String> tuple : rankings) {
            String userId = tuple.getValue();
            Double score = tuple.getScore();

            String userKey = RedisKeyUtil.sessionUserKey(sessionId, Long.parseLong(userId));
            Map<Object, Object> userData = redisTemplate.opsForHash().entries(userKey);

            entries.add(LeaderboardEntry.builder()
                    .rank(rank++)
                    .userId(Long.parseLong(userId))
                    .score(score != null ? score.intValue() : 0)
                    .solvedCount(parseIntOrZero(userData.get("solved_count")))
                    .streak(parseIntOrZero(userData.get("streak")))
                    .build());
        }

        return entries;
    }

    public void addScore(Long userId, int score) {
        // Global ranking
        redisTemplate.opsForZSet().incrementScore(
                RedisKeyUtil.globalRankingKey(),
                String.valueOf(userId),
                score
        );

        // Weekly ranking
        redisTemplate.opsForZSet().incrementScore(
                RedisKeyUtil.weeklyRankingKey(),
                String.valueOf(userId),
                score
        );

        // Monthly ranking
        redisTemplate.opsForZSet().incrementScore(
                RedisKeyUtil.monthlyRankingKey(),
                String.valueOf(userId),
                score
        );
    }

    public void deductScore(String sessionId, Long userId, int score) {
        String lbKey = RedisKeyUtil.sessionLeaderboardKey(sessionId);
        redisTemplate.opsForZSet().incrementScore(lbKey, String.valueOf(userId), -score);

        String userKey = RedisKeyUtil.sessionUserKey(sessionId, userId);
        redisTemplate.opsForHash().increment(userKey, "score", -score);
    }

    public RankingResponse getGlobalRanking(int limit) {
        return getRanking(RedisKeyUtil.globalRankingKey(), "global", limit);
    }

    public RankingResponse getWeeklyRanking(int limit) {
        return getRanking(RedisKeyUtil.weeklyRankingKey(), "weekly", limit);
    }

    public RankingResponse getMonthlyRanking(int limit) {
        return getRanking(RedisKeyUtil.monthlyRankingKey(), "monthly", limit);
    }

    private RankingResponse getRanking(String key, String type, int limit) {
        Set<ZSetOperations.TypedTuple<String>> rankings = redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, 0, limit - 1);

        Long totalParticipants = redisTemplate.opsForZSet().zCard(key);

        List<LeaderboardEntry> entries = new ArrayList<>();
        if (rankings != null) {
            int rank = 1;
            for (ZSetOperations.TypedTuple<String> tuple : rankings) {
                entries.add(LeaderboardEntry.builder()
                        .rank(rank++)
                        .userId(Long.parseLong(tuple.getValue()))
                        .score(tuple.getScore() != null ? tuple.getScore().intValue() : 0)
                        .build());
            }
        }

        return RankingResponse.builder()
                .type(type)
                .rankings(entries)
                .totalParticipants(totalParticipants != null ? totalParticipants.intValue() : 0)
                .build();
    }

    public LeaderboardEntry getMyRanking(Long userId, String type) {
        String key = switch (type) {
            case "weekly" -> RedisKeyUtil.weeklyRankingKey();
            case "monthly" -> RedisKeyUtil.monthlyRankingKey();
            default -> RedisKeyUtil.globalRankingKey();
        };

        Long rank = redisTemplate.opsForZSet().reverseRank(key, String.valueOf(userId));
        Double score = redisTemplate.opsForZSet().score(key, String.valueOf(userId));

        if (rank == null) {
            return LeaderboardEntry.builder()
                    .userId(userId)
                    .rank(null)
                    .score(0)
                    .build();
        }

        return LeaderboardEntry.builder()
                .userId(userId)
                .rank(rank.intValue() + 1)
                .score(score != null ? score.intValue() : 0)
                .build();
    }

    // Reset weekly ranking every Monday at 00:00
    @Scheduled(cron = "0 0 0 * * MON", zone = "Asia/Seoul")
    public void resetWeeklyRanking() {
        log.info("Resetting weekly ranking");
        redisTemplate.delete(RedisKeyUtil.weeklyRankingKey());
    }

    // Reset monthly ranking on the 1st of every month at 00:00
    @Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Seoul")
    public void resetMonthlyRanking() {
        log.info("Resetting monthly ranking");
        redisTemplate.delete(RedisKeyUtil.monthlyRankingKey());
    }

    private int parseIntOrZero(Object value) {
        if (value == null) return 0;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
