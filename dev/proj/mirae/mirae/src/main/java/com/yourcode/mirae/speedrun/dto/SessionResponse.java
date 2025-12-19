package com.yourcode.mirae.speedrun.dto;

import com.yourcode.mirae.speedrun.redis.GameMode;
import com.yourcode.mirae.speedrun.redis.SessionState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private String sessionId;
    private GameMode mode;
    private SessionState state;
    private Integer duration;
    private Integer maxPlayers;
    private Integer currentPlayers;
    private Long createdBy;
    private Long startedAt;
    private Long endedAt;
    private String difficultyRange;
    private List<String> tags;
}
