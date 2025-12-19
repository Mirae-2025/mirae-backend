package com.yourcode.mirae.usermetadata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationItem {
    @JsonProperty("problem_id")
    private Integer problemId;

    private String title;
    private String difficulty;
    private Double accuracy;
    private Double score;
    private String reason;
}
