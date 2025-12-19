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
public class BatchRecommendRequest {
    @JsonProperty("user_ids")
    private List<String> userIds;

    @Builder.Default
    private Integer k = 5;

    @JsonProperty("exclude_solved")
    @Builder.Default
    private Boolean excludeSolved = true;

    @JsonProperty("difficulty_min")
    private String difficultyMin;

    @JsonProperty("difficulty_max")
    private String difficultyMax;
}
