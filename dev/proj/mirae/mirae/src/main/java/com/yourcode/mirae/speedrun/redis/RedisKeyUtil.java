package com.yourcode.mirae.speedrun.redis;

public final class RedisKeyUtil {

    private RedisKeyUtil() {
    }

    public static final long SESSION_TTL_SECONDS = 24 * 60 * 60; // 24 hours

    private static final String SESSION_PREFIX = "speed:session:";
    private static final String LEADERBOARD_PREFIX = "speed:lb:";
    private static final String GLOBAL_RANKING = "speed:global:ranking";
    private static final String WEEKLY_RANKING = "speed:global:ranking:weekly";
    private static final String MONTHLY_RANKING = "speed:global:ranking:monthly";
    private static final String PENALTY_PREFIX = "user:penalty:";

    public static String sessionKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }

    public static String sessionPlayersKey(String sessionId) {
        return SESSION_PREFIX + sessionId + ":players";
    }

    public static String sessionUserKey(String sessionId, Long userId) {
        return SESSION_PREFIX + sessionId + ":user:" + userId;
    }

    public static String sessionProblemsKey(String sessionId) {
        return SESSION_PREFIX + sessionId + ":problems";
    }

    public static String sessionClaimsKey(String sessionId, Long userId) {
        return SESSION_PREFIX + sessionId + ":claims:" + userId;
    }

    public static String sessionLeaderboardKey(String sessionId) {
        return LEADERBOARD_PREFIX + sessionId;
    }

    public static String globalRankingKey() {
        return GLOBAL_RANKING;
    }

    public static String weeklyRankingKey() {
        return WEEKLY_RANKING;
    }

    public static String monthlyRankingKey() {
        return MONTHLY_RANKING;
    }

    public static String userPenaltyKey(Long userId) {
        return PENALTY_PREFIX + userId;
    }

    // Legacy aliases for compatibility
    public static String playersKey(String sessionId) {
        return sessionPlayersKey(sessionId);
    }

    public static String userKey(String sessionId, Long userId) {
        return sessionUserKey(sessionId, userId);
    }

    public static String problemsKey(String sessionId) {
        return sessionProblemsKey(sessionId);
    }

    public static String claimsKey(String sessionId, Long userId) {
        return sessionClaimsKey(sessionId, userId);
    }

    public static String leaderboardKey(String sessionId) {
        return sessionLeaderboardKey(sessionId);
    }

    public static String penaltyKey(Long userId) {
        return userPenaltyKey(userId);
    }
}
