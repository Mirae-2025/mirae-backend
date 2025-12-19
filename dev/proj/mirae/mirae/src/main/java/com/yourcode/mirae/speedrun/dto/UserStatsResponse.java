package com.yourcode.mirae.speedrun.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {
    private Long userId;
    private Integer totalGames;
    private Double averageScore;
    private Integer totalProblemsSolved;
    private Integer maxStreak;
}
