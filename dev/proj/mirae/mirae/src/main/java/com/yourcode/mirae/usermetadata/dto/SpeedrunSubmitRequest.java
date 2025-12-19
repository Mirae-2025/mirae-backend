package com.yourcode.mirae.usermetadata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class SpeedrunSubmitRequest {
    @JsonProperty("session_id")
    @NotBlank(message = "Session ID is required")
    private String sessionId;

    @JsonProperty("problem_id")
    @NotNull(message = "Problem ID is required")
    private Integer problemId;

    @NotBlank(message = "Verdict is required")
    private String verdict;
}
