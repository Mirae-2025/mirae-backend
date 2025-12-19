package com.yourcode.mirae.speedrun.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitRequest {

    @NotNull(message = "Problem ID is required")
    private Long problemId;

    private Integer tier; // solved.ac tier (0-30)
}
