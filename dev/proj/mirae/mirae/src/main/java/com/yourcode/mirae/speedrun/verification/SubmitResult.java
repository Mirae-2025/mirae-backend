package com.yourcode.mirae.speedrun.verification;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SubmitResult {
    private boolean correct;
    private LocalDateTime submitTime;
    private String memory;
    private String executionTime;
}
