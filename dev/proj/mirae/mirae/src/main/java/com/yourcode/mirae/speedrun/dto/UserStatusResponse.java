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
public class UserStatusResponse {
    private Long userId;
    private Integer score;
    private Integer solvedCount;
    private Integer wrongCount;
    private Integer streak;
    private Integer currentRank;
    private Long lastSubmitAt;
    private List<Long> solvedProblems;
}
