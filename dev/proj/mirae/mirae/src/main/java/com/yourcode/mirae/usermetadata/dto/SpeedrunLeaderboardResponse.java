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
public class SpeedrunLeaderboardResponse {
    private String mode;
    private List<LeaderboardEntry> entries;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaderboardEntry {
        @JsonProperty("user_id")
        private String userId;

        private Integer score;

        @JsonProperty("solved_count")
        private Integer solvedCount;

        @JsonProperty("total_count")
        private Integer totalCount;

        @JsonProperty("completed_at")
        private String completedAt;
    }
}
