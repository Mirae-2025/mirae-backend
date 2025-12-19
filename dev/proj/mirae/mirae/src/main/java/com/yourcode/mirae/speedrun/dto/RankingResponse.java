package com.yourcode.mirae.speedrun.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingResponse {
    private String type; // "global", "weekly", "monthly"
    private List<LeaderboardEntry> rankings;
    private Integer totalParticipants;
}
