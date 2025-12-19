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
public class SpeedrunResultResponse {
    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("user_id")
    private String userId;

    private String mode;

    @JsonProperty("total_time")
    private Integer totalTime;

    @JsonProperty("solved_count")
    private Integer solvedCount;

    @JsonProperty("total_count")
    private Integer totalCount;

    @JsonProperty("final_score")
    private Integer finalScore;

    @JsonProperty("problems_detail")
    private List<ProblemDetail> problemsDetail;

    private Integer rank;
    private String status;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProblemDetail {
        @JsonProperty("problem_id")
        private Integer problemId;
        private Boolean solved;
    }
}
