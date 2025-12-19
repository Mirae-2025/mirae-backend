package com.yourcode.mirae.speedrun.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntry {
    private Integer rank;
    private Long userId;
    private String username;
    private Integer score;
    private Integer solvedCount;
    private Integer streak;
}
