package com.yourcode.mirae.speedrun.dto;

import com.yourcode.mirae.speedrun.redis.SessionState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionStateResponse {
    private String sessionId;
    private SessionState state;
    private Long remainingTimeMs;
    private Long timestamp;
}
