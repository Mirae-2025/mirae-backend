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
public class SpeedrunSubmitResponse {
    private Boolean success;
    private Integer score;

    @JsonProperty("solved_count")
    private Integer solvedCount;

    private List<Integer> remaining;
    private String error;
}
