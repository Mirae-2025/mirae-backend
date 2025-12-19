package com.yourcode.mirae.speedrun.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitResponse {
    private boolean success;
    private Long problemId;
    private Integer scoreEarned;
    private Integer totalScore;
    private Integer currentRank;
    private Integer solvedCount;
    private Integer streak; // For Retry mode
    private String message;
}
