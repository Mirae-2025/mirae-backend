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
public class AccuracyTrendResponse {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("period_days")
    private Integer periodDays;

    @JsonProperty("weekly_trend")
    private List<GrowthReportResponse.WeeklyStats> weeklyTrend;

    private Double improvement;
}
