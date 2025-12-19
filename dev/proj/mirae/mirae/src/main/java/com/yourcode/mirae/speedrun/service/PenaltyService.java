package com.yourcode.mirae.speedrun.service;

import com.yourcode.mirae.speedrun.redis.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PenaltyService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_FALSE_CLAIMS = 3;
    private static final int PENALTY_EXPIRE_DAYS = 30;

    public void addFalseClaimPenalty(Long userId) {
        String key = RedisKeyUtil.userPenaltyKey(userId);
        Long count = redisTemplate.opsForValue().increment(key);

        redisTemplate.expire(key, PENALTY_EXPIRE_DAYS, TimeUnit.DAYS);

        if (count != null && count >= MAX_FALSE_CLAIMS) {
            log.warn("User {} has reached {} false claims, consider suspension", userId, count);
        }
    }

    public int getFalseClaimCount(Long userId) {
        String key = RedisKeyUtil.userPenaltyKey(userId);
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    public void resetPenalty(Long userId) {
        String key = RedisKeyUtil.userPenaltyKey(userId);
        redisTemplate.delete(key);
    }

    public boolean isUserSuspended(Long userId) {
        return getFalseClaimCount(userId) >= MAX_FALSE_CLAIMS;
    }
}
