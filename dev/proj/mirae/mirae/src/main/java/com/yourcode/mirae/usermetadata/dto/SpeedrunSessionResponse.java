package com.yourcode.mirae.usermetadata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeedrunSessionResponse {
    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("user_id")
    private String userId;

    private String mode;
    private String difficulty;

    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("end_time")
    private String endTime;

    @JsonProperty("remaining_seconds")
    private Integer remainingSeconds;

    @JsonProperty("problem_set_id")
    private String problemSetId;

    private List<Integer> problems;

    @JsonProperty("problem_scores")
    private List<Integer> problemScores;

    @JsonProperty("solved_problems")
    private List<Integer> solvedProblems;

    @JsonProperty("solved_count")
    private Integer solvedCount;

    @JsonProperty("total_count")
    private Integer totalCount;

    private String status;
    private Integer score;
}
