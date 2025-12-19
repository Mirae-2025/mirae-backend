package com.yourcode.mirae.speedrun.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionRequest {

    @NotNull(message = "Duration is required")
    @Min(value = 5, message = "Minimum duration is 5 minutes")
    @Max(value = 120, message = "Maximum duration is 120 minutes")
    private Integer duration;

    @Min(value = 1, message = "Minimum players is 1")
    @Max(value = 100, message = "Maximum players is 100")
    @Builder.Default
    private Integer maxPlayers = 10;

    private String difficultyRange; // "bronze", "silver", "gold", "platinum", "all"

    private List<String> tags; // For TagFocus mode
}
