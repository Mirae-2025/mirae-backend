package com.yourcode.mirae.speedrun.service;

import com.yourcode.mirae.speedrun.redis.GameMode;
import org.springframework.stereotype.Service;

@Service
public class ScoreCalculationService {

    public int getBaseScore(int tier) {
        // tier: 0 = Unrated, 1-5 = Bronze, 6-10 = Silver, 11-15 = Gold,
        // 16-20 = Platinum, 21-25 = Diamond, 26-30 = Ruby
        if (tier == 0) return 5;           // Unrated
        if (tier <= 5) return 10;          // Bronze
        if (tier <= 10) return 25;         // Silver
        if (tier <= 15) return 50;         // Gold
        if (tier <= 20) return 100;        // Platinum
        if (tier <= 25) return 200;        // Diamond
        return 500;                         // Ruby
    }

    public double getTimeBonus(long elapsedMs, long totalDurationMs) {
        if (totalDurationMs <= 0) return 1.0;
        double timeRatio = (double) elapsedMs / totalDurationMs;
        timeRatio = Math.min(1.0, Math.max(0.0, timeRatio));
        return 1.0 + (0.5 * (1 - timeRatio)); // 1.0 ~ 1.5
    }

    public double getStreakMultiplier(int streak) {
        return switch (streak) {
            case 0, 1 -> 1.0;
            case 2 -> 1.2;
            case 3 -> 1.5;
            case 4 -> 1.8;
            default -> 2.0;
        };
    }

    public int calculateScore(GameMode mode, int tier, long elapsedMs, long durationMs, int streak) {
        int baseScore = getBaseScore(tier);
        double timeBonus = getTimeBonus(elapsedMs, durationMs);
        double streakBonus = (mode == GameMode.RETRY) ? getStreakMultiplier(streak) : 1.0;
        return (int) Math.round(baseScore * timeBonus * streakBonus);
    }

    public String getTierName(int tier) {
        if (tier == 0) return "Unrated";
        String[] levels = {"5", "4", "3", "2", "1"};
        String level = levels[(tier - 1) % 5];

        if (tier <= 5) return "Bronze " + level;
        if (tier <= 10) return "Silver " + level;
        if (tier <= 15) return "Gold " + level;
        if (tier <= 20) return "Platinum " + level;
        if (tier <= 25) return "Diamond " + level;
        return "Ruby " + level;
    }
}
