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
public class WeakTag {
    private String tag;

    @JsonProperty("success_rate")
    private Double successRate;

    private Integer failures;

    @JsonProperty("total_attempts")
    private Integer totalAttempts;
}
