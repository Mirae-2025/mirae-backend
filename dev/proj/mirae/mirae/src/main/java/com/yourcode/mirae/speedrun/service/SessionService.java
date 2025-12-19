package com.yourcode.mirae.speedrun.service;

import com.yourcode.mirae.speedrun.dto.*;
import com.yourcode.mirae.speedrun.entity.SpeedrunResult;
import com.yourcode.mirae.speedrun.entity.SpeedrunSession;
import com.yourcode.mirae.speedrun.exception.*;
import com.yourcode.mirae.speedrun.redis.ClaimStatus;
import com.yourcode.mirae.speedrun.redis.GameMode;
import com.yourcode.mirae.speedrun.redis.RedisKeyUtil;
import com.yourcode.mirae.speedrun.redis.SessionState;
import com.yourcode.mirae.speedrun.repository.SpeedrunResultRepository;
import com.yourcode.mirae.speedrun.repository.SpeedrunSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SpeedrunSessionRepository sessionRepository;
    private final SpeedrunResultRepository resultRepository;
    private final ScoreCalculationService scoreService;
    private final RankingService rankingService;

    public SessionResponse createSession(GameMode mode, CreateSessionRequest request, Long userId) {
        String sessionId = UUID.randomUUID().toString();
        String sessionKey = RedisKeyUtil.sessionKey(sessionId);

        Map<String, String> sessionData = new HashMap<>();
        sessionData.put("state", SessionState.WAITING.name());
        sessionData.put("mode", mode.name());
        sessionData.put("duration", String.valueOf(request.getDuration()));
        sessionData.put("max_players", String.valueOf(request.getMaxPlayers()));
        sessionData.put("created_by", String.valueOf(userId));
        sessionData.put("created_at", String.valueOf(System.currentTimeMillis()));

        if (request.getDifficultyRange() != null) {
            sessionData.put("difficulty_range", request.getDifficultyRange());
        }

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            sessionData.put("tags", String.join(",", request.getTags()));
        }

        redisTemplate.opsForHash().putAll(sessionKey, sessionData);
        redisTemplate.expire(sessionKey, RedisKeyUtil.SESSION_TTL_SECONDS, TimeUnit.SECONDS);

        // Auto-join the creator
        joinSessionInternal(sessionId, userId);

        return buildSessionResponse(sessionId);
    }

    public SessionResponse joinSession(String sessionId, Long userId) {
        validateSession(sessionId);
        SessionState state = getSessionState(sessionId);

        if (state != SessionState.WAITING) {
            throw new SessionNotRunningException(sessionId);
        }

        String playersKey = RedisKeyUtil.sessionPlayersKey(sessionId);
        Boolean isMember = redisTemplate.opsForSet().isMember(playersKey, String.valueOf(userId));

        if (Boolean.TRUE.equals(isMember)) {
            throw new AlreadyJoinedException(sessionId, userId);
        }

        int maxPlayers = getMaxPlayers(sessionId);
        Long currentPlayers = redisTemplate.opsForSet().size(playersKey);

        if (currentPlayers != null && currentPlayers >= maxPlayers) {
            throw new SessionFullException(sessionId);
        }

        joinSessionInternal(sessionId, userId);
        return buildSessionResponse(sessionId);
    }

    private void joinSessionInternal(String sessionId, Long userId) {
        String playersKey = RedisKeyUtil.sessionPlayersKey(sessionId);
        String userKey = RedisKeyUtil.sessionUserKey(sessionId, userId);

        redisTemplate.opsForSet().add(playersKey, String.valueOf(userId));
        redisTemplate.expire(playersKey, RedisKeyUtil.SESSION_TTL_SECONDS, TimeUnit.SECONDS);

        Map<String, String> userData = new HashMap<>();
        userData.put("score", "0");
        userData.put("solved_count", "0");
        userData.put("wrong_count", "0");
        userData.put("streak", "0");
        userData.put("solved_problems", "");

        redisTemplate.opsForHash().putAll(userKey, userData);
        redisTemplate.expire(userKey, RedisKeyUtil.SESSION_TTL_SECONDS, TimeUnit.SECONDS);

        // Add to leaderboard with 0 score
        String lbKey = RedisKeyUtil.sessionLeaderboardKey(sessionId);
        redisTemplate.opsForZSet().add(lbKey, String.valueOf(userId), 0);
        redisTemplate.expire(lbKey, RedisKeyUtil.SESSION_TTL_SECONDS, TimeUnit.SECONDS);
    }

    public SessionResponse startSession(String sessionId, Long userId) {
        validateSession(sessionId);

        Long createdBy = getCreatedBy(sessionId);
        if (!createdBy.equals(userId)) {
            throw new NotSessionOwnerException(sessionId);
        }

        SessionState state = getSessionState(sessionId);
        if (state != SessionState.WAITING) {
            throw new SessionNotRunningException(sessionId);
        }

        String sessionKey = RedisKeyUtil.sessionKey(sessionId);
        long startAt = System.currentTimeMillis();
        int duration = getDuration(sessionId);
        long endAt = startAt + (duration * 60 * 1000L);

        redisTemplate.opsForHash().put(sessionKey, "state", SessionState.RUNNING.name());
        redisTemplate.opsForHash().put(sessionKey, "start_at", String.valueOf(startAt));
        redisTemplate.opsForHash().put(sessionKey, "end_at", String.valueOf(endAt));

        return buildSessionResponse(sessionId);
    }

    public SessionResponse leaveSession(String sessionId, Long userId) {
        validateSession(sessionId);

        String playersKey = RedisKeyUtil.sessionPlayersKey(sessionId);
        Boolean isMember = redisTemplate.opsForSet().isMember(playersKey, String.valueOf(userId));

        if (Boolean.FALSE.equals(isMember)) {
            throw new UserNotInSessionException(sessionId, userId);
        }

        redisTemplate.opsForSet().remove(playersKey, String.valueOf(userId));

        String userKey = RedisKeyUtil.sessionUserKey(sessionId, userId);
        redisTemplate.delete(userKey);

        String lbKey = RedisKeyUtil.sessionLeaderboardKey(sessionId);
        redisTemplate.opsForZSet().remove(lbKey, String.valueOf(userId));

        return buildSessionResponse(sessionId);
    }

    public SubmitResponse submitProblem(String sessionId, Long userId, SubmitRequest request) {
        validateSession(sessionId);

        SessionState state = getSessionState(sessionId);
        if (state != SessionState.RUNNING) {
            throw new SessionNotRunningException(sessionId);
        }

        String playersKey = RedisKeyUtil.sessionPlayersKey(sessionId);
        Boolean isMember = redisTemplate.opsForSet().isMember(playersKey, String.valueOf(userId));
        if (Boolean.FALSE.equals(isMember)) {
            throw new UserNotInSessionException(sessionId, userId);
        }

        // Check session time
        Long endAt = getEndAt(sessionId);
        if (endAt != null && System.currentTimeMillis() > endAt) {
            throw new SessionExpiredException(sessionId);
        }

        String userKey = RedisKeyUtil.sessionUserKey(sessionId, userId);
        String solvedProblems = (String) redisTemplate.opsForHash().get(userKey, "solved_problems");

        if (solvedProblems != null && !solvedProblems.isEmpty()) {
            List<String> solved = Arrays.asList(solvedProblems.split(","));
            if (solved.contains(String.valueOf(request.getProblemId()))) {
                throw new DuplicateSubmitException(request.getProblemId());
            }
        }

        // Calculate score
        GameMode mode = getMode(sessionId);
        Long startAt = getStartAt(sessionId);
        int duration = getDuration(sessionId);
        long elapsedMs = System.currentTimeMillis() - (startAt != null ? startAt : 0);
        long durationMs = duration * 60 * 1000L;

        int currentStreak = getIntField(userKey, "streak");
        int newStreak = currentStreak + 1;
        int tier = request.getTier() != null ? request.getTier() : 0;
        int scoreEarned = scoreService.calculateScore(mode, tier, elapsedMs, durationMs, newStreak);

        // Update user data
        redisTemplate.opsForHash().increment(userKey, "score", scoreEarned);
        redisTemplate.opsForHash().increment(userKey, "solved_count", 1);
        redisTemplate.opsForHash().put(userKey, "streak", String.valueOf(newStreak));
        redisTemplate.opsForHash().put(userKey, "last_submit_at", String.valueOf(System.currentTimeMillis()));

        String newSolvedProblems = (solvedProblems == null || solvedProblems.isEmpty())
                ? String.valueOf(request.getProblemId())
                : solvedProblems + "," + request.getProblemId();
        redisTemplate.opsForHash().put(userKey, "solved_problems", newSolvedProblems);

        // Save claim status (PENDING until verified)
        String claimsKey = RedisKeyUtil.sessionClaimsKey(sessionId, userId);
        redisTemplate.opsForHash().put(claimsKey, String.valueOf(request.getProblemId()), ClaimStatus.PENDING.name());
        redisTemplate.expire(claimsKey, RedisKeyUtil.SESSION_TTL_SECONDS, TimeUnit.SECONDS);

        // Update leaderboard
        String lbKey = RedisKeyUtil.sessionLeaderboardKey(sessionId);
        redisTemplate.opsForZSet().incrementScore(lbKey, String.valueOf(userId), scoreEarned);

        // Update global rankings
        rankingService.addScore(userId, scoreEarned);

        int totalScore = getIntField(userKey, "score");
        int solvedCount = getIntField(userKey, "solved_count");
        Long rank = redisTemplate.opsForZSet().reverseRank(lbKey, String.valueOf(userId));

        return SubmitResponse.builder()
                .success(true)
                .problemId(request.getProblemId())
                .scoreEarned(scoreEarned)
                .totalScore(totalScore)
                .currentRank(rank != null ? rank.intValue() + 1 : 1)
                .solvedCount(solvedCount)
                .streak(newStreak)
                .message("Problem submitted successfully")
                .build();
    }

    public UserStatusResponse getUserStatus(String sessionId, Long userId) {
        validateSession(sessionId);

        String userKey = RedisKeyUtil.sessionUserKey(sessionId, userId);
        Map<Object, Object> userData = redisTemplate.opsForHash().entries(userKey);

        if (userData.isEmpty()) {
            throw new UserNotInSessionException(sessionId, userId);
        }

        String lbKey = RedisKeyUtil.sessionLeaderboardKey(sessionId);
        Long rank = redisTemplate.opsForZSet().reverseRank(lbKey, String.valueOf(userId));

        String solvedStr = (String) userData.get("solved_problems");
        List<Long> solvedProblems = new ArrayList<>();
        if (solvedStr != null && !solvedStr.isEmpty()) {
            solvedProblems = Arrays.stream(solvedStr.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }

        return UserStatusResponse.builder()
                .userId(userId)
                .score(parseIntOrZero(userData.get("score")))
                .solvedCount(parseIntOrZero(userData.get("solved_count")))
                .wrongCount(parseIntOrZero(userData.get("wrong_count")))
                .streak(parseIntOrZero(userData.get("streak")))
                .currentRank(rank != null ? rank.intValue() + 1 : null)
                .lastSubmitAt(parseLongOrNull(userData.get("last_submit_at")))
                .solvedProblems(solvedProblems)
                .build();
    }

    public SessionResponse getSession(String sessionId) {
        validateSession(sessionId);
        return buildSessionResponse(sessionId);
    }

    public SessionStateResponse getSessionState(String sessionId, Long userId) {
        validateSession(sessionId);

        SessionState state = getSessionState(sessionId);
        Long endAt = getEndAt(sessionId);
        long remainingMs = 0;

        if (state == SessionState.RUNNING && endAt != null) {
            remainingMs = Math.max(0, endAt - System.currentTimeMillis());
            if (remainingMs == 0) {
                endSession(sessionId);
                state = SessionState.ENDED;
            }
        }

        return SessionStateResponse.builder()
                .sessionId(sessionId)
                .state(state)
                .remainingTimeMs(remainingMs)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @Transactional
    public void endSession(String sessionId) {
        String sessionKey = RedisKeyUtil.sessionKey(sessionId);
        redisTemplate.opsForHash().put(sessionKey, "state", SessionState.ENDED.name());
        redisTemplate.opsForHash().put(sessionKey, "ended_at", String.valueOf(System.currentTimeMillis()));

        saveSessionToMySQL(sessionId);
    }

    private void saveSessionToMySQL(String sessionId) {
        String sessionKey = RedisKeyUtil.sessionKey(sessionId);
        Map<Object, Object> sessionData = redisTemplate.opsForHash().entries(sessionKey);

        if (sessionData.isEmpty()) {
            return;
        }

        SpeedrunSession session = SpeedrunSession.builder()
                .sessionId(sessionId)
                .mode(GameMode.valueOf((String) sessionData.get("mode")))
                .duration(parseIntOrZero(sessionData.get("duration")))
                .tags((String) sessionData.get("tags"))
                .difficultyRange((String) sessionData.get("difficulty_range"))
                .createdBy(parseLongOrNull(sessionData.get("created_by")))
                .maxPlayers(parseIntOrZero(sessionData.get("max_players")))
                .startedAt(parseTimestamp(sessionData.get("start_at")))
                .endedAt(parseTimestamp(sessionData.get("ended_at")))
                .build();

        String playersKey = RedisKeyUtil.sessionPlayersKey(sessionId);
        Long totalParticipants = redisTemplate.opsForSet().size(playersKey);
        session.setTotalParticipants(totalParticipants != null ? totalParticipants.intValue() : 0);

        sessionRepository.save(session);

        // Save results
        saveResultsToMySQL(sessionId);
    }

    private void saveResultsToMySQL(String sessionId) {
        String lbKey = RedisKeyUtil.sessionLeaderboardKey(sessionId);
        Set<String> players = redisTemplate.opsForZSet().reverseRange(lbKey, 0, -1);

        if (players == null || players.isEmpty()) {
            return;
        }

        int rank = 1;
        for (String userIdStr : players) {
            Long userId = Long.parseLong(userIdStr);
            String userKey = RedisKeyUtil.sessionUserKey(sessionId, userId);
            Map<Object, Object> userData = redisTemplate.opsForHash().entries(userKey);

            SpeedrunResult result = SpeedrunResult.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .finalScore(parseIntOrZero(userData.get("score")))
                    .solvedCount(parseIntOrZero(userData.get("solved_count")))
                    .wrongCount(parseIntOrZero(userData.get("wrong_count")))
                    .finalRank(rank++)
                    .maxStreak(parseIntOrZero(userData.get("streak")))
                    .solvedProblems((String) userData.get("solved_problems"))
                    .build();

            resultRepository.save(result);
        }
    }

    private void validateSession(String sessionId) {
        String sessionKey = RedisKeyUtil.sessionKey(sessionId);
        Boolean exists = redisTemplate.hasKey(sessionKey);
        if (Boolean.FALSE.equals(exists)) {
            throw new SessionNotFoundException(sessionId);
        }
    }

    private SessionResponse buildSessionResponse(String sessionId) {
        String sessionKey = RedisKeyUtil.sessionKey(sessionId);
        Map<Object, Object> data = redisTemplate.opsForHash().entries(sessionKey);

        String playersKey = RedisKeyUtil.sessionPlayersKey(sessionId);
        Long playerCount = redisTemplate.opsForSet().size(playersKey);

        List<String> tags = null;
        String tagsStr = (String) data.get("tags");
        if (tagsStr != null && !tagsStr.isEmpty()) {
            tags = Arrays.asList(tagsStr.split(","));
        }

        return SessionResponse.builder()
                .sessionId(sessionId)
                .mode(GameMode.valueOf((String) data.get("mode")))
                .state(SessionState.valueOf((String) data.get("state")))
                .duration(parseIntOrZero(data.get("duration")))
                .maxPlayers(parseIntOrZero(data.get("max_players")))
                .currentPlayers(playerCount != null ? playerCount.intValue() : 0)
                .createdBy(parseLongOrNull(data.get("created_by")))
                .startedAt(parseLongOrNull(data.get("start_at")))
                .endedAt(parseLongOrNull(data.get("end_at")))
                .difficultyRange((String) data.get("difficulty_range"))
                .tags(tags)
                .build();
    }

    private SessionState getSessionState(String sessionId) {
        String sessionKey = RedisKeyUtil.sessionKey(sessionId);
        String state = (String) redisTemplate.opsForHash().get(sessionKey, "state");
        return state != null ? SessionState.valueOf(state) : SessionState.WAITING;
    }

    private GameMode getMode(String sessionId) {
        String sessionKey = RedisKeyUtil.sessionKey(sessionId);
        String mode = (String) redisTemplate.opsForHash().get(sessionKey, "mode");
        return mode != null ? GameMode.valueOf(mode) : GameMode.CLASSIC;
    }

    private int getDuration(String sessionId) {
        String sessionKey = RedisKeyUtil.sessionKey(sessionId);
        return parseIntOrZero(redisTemplate.opsForHash().get(sessionKey, "duration"));
    }

    private int getMaxPlayers(String sessionId) {
        String sessionKey = RedisKeyUtil.sessionKey(sessionId);
        return parseIntOrZero(redisTemplate.opsForHash().get(sessionKey, "max_players"));
    }

    private Long getCreatedBy(String sessionId) {
        String sessionKey = RedisKeyUtil.sessionKey(sessionId);
        return parseLongOrNull(redisTemplate.opsForHash().get(sessionKey, "created_by"));
    }

    private Long getStartAt(String sessionId) {
        String sessionKey = RedisKeyUtil.sessionKey(sessionId);
        return parseLongOrNull(redisTemplate.opsForHash().get(sessionKey, "start_at"));
    }

    private Long getEndAt(String sessionId) {
        String sessionKey = RedisKeyUtil.sessionKey(sessionId);
        return parseLongOrNull(redisTemplate.opsForHash().get(sessionKey, "end_at"));
    }

    private int getIntField(String key, String field) {
        return parseIntOrZero(redisTemplate.opsForHash().get(key, field));
    }

    private int parseIntOrZero(Object value) {
        if (value == null) return 0;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Long parseLongOrNull(Object value) {
        if (value == null) return null;
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime parseTimestamp(Object value) {
        Long ts = parseLongOrNull(value);
        if (ts == null) return null;
        return LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(ts),
                java.time.ZoneId.of("Asia/Seoul")
        );
    }
}
