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
public class GrowthReportResponse {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("period_days")
    private Integer periodDays;

    @JsonProperty("total_attempts")
    private Integer totalAttempts;

    @JsonProperty("total_solved")
    private Integer totalSolved;

    @JsonProperty("overall_accuracy")
    private Double overallAccuracy;

    @JsonProperty("weak_tags")
    private List<TagAccuracy> weakTags;

    @JsonProperty("strong_tags")
    private List<TagAccuracy> strongTags;

    @JsonProperty("difficulty_stats")
    private List<DifficultyProgress> difficultyStats;

    @JsonProperty("weekly_progress")
    private List<WeeklyStats> weeklyProgress;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagAccuracy {
        private String tag;
        private Integer total;
        private Integer solved;
        private Double accuracy;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DifficultyProgress {
        private String difficulty;
        private Integer total;
        private Integer solved;
        private Double accuracy;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyStats {
        private String week;

        @JsonProperty("total_attempts")
        private Integer totalAttempts;

        @JsonProperty("total_solved")
        private Integer totalSolved;

        private Double accuracy;
    }
}
