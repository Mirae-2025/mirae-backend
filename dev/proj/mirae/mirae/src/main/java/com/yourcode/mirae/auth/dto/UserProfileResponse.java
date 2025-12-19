package com.yourcode.mirae.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserProfileResponse {
    private String uuid;
    private String email;
    private String baekjoonId;
    private String githubId;
    private Integer solvedCount;
    private String tier;
    private LocalDateTime createdAt;
}
