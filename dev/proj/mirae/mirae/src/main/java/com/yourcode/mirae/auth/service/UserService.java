package com.yourcode.mirae.auth.service;

import com.yourcode.mirae.auth.dto.UserProfileResponse;
import com.yourcode.mirae.auth.entity.User;
import com.yourcode.mirae.auth.exception.AuthException;
import com.yourcode.mirae.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String uuid) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new AuthException("USER_NOT_FOUND", "User not found"));

        return UserProfileResponse.builder()
                .uuid(user.getUuid())
                .email(user.getEmail())
                .baekjoonId(user.getBaekjunId())
                .githubId(user.getGithubId())
                .solvedCount(user.getSolvedCount())
                .tier(user.getTier())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("USER_NOT_FOUND", "User not found"));

        return UserProfileResponse.builder()
                .uuid(user.getUuid())
                .email(user.getEmail())
                .baekjoonId(user.getBaekjunId())
                .githubId(user.getGithubId())
                .solvedCount(user.getSolvedCount())
                .tier(user.getTier())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
