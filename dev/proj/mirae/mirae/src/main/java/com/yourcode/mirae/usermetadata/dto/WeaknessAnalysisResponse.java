package com.yourcode.mirae.usermetadata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeaknessAnalysisResponse {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("total_attempts")
    private Integer totalAttempts;

    @JsonProperty("total_failures")
    private Integer totalFailures;

    @JsonProperty("weak_tags")
    private List<WeakTag> weakTags;

    @JsonProperty("tag_stats")
    private Map<String, TagStat> tagStats;

    @JsonProperty("recent_failures")
    private List<FailedProblem> recentFailures;
}
