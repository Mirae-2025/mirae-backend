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
public class TagAnalysisResponse {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("total_tags")
    private Integer totalTags;

    @JsonProperty("tag_stats")
    private List<GrowthReportResponse.TagAccuracy> tagStats;

    @JsonProperty("most_attempted")
    private List<GrowthReportResponse.TagAccuracy> mostAttempted;

    @JsonProperty("needs_improvement")
    private List<GrowthReportResponse.TagAccuracy> needsImprovement;
}
