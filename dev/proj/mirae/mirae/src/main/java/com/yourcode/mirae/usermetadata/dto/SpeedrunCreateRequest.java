package com.yourcode.mirae.usermetadata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class SpeedrunCreateRequest {
    @JsonProperty("user_id")
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Mode is required")
    @Pattern(regexp = "30min|60min|90min|120min", message = "Mode must be one of: 30min, 60min, 90min, 120min")
    private String mode;

    @NotBlank(message = "Difficulty is required")
    @Pattern(regexp = "bronze|silver|gold|platinum", message = "Difficulty must be one of: bronze, silver, gold, platinum")
    private String difficulty;
}
