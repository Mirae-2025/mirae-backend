package com.yourcode.mirae.auth.service;

import com.yourcode.mirae.auth.exception.AuthException;
import com.yourcode.mirae.auth.dto.LoginRequest;
import com.yourcode.mirae.auth.dto.LoginResponse;
import com.yourcode.mirae.auth.dto.SignupRequest;
import com.yourcode.mirae.auth.dto.SignupResponse;
import com.yourcode.mirae.auth.dto.TokenRefreshRequest;
import com.yourcode.mirae.auth.dto.TokenRefreshResponse;
import com.yourcode.mirae.auth.entity.User;
import com.yourcode.mirae.auth.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("INVALID_CREDENTIALS", "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException("INVALID_CREDENTIALS", "Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(
                user.getUuid(),
                user.getEmail(),
                user.getRole().name()
        );
        String refreshToken = jwtService.generateRefreshToken(user.getUuid());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenValidity() / 1000)
                .uuid(user.getUuid())
                .email(user.getEmail())
                .build();
    }

    @Transactional(readOnly = true)
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        try {
            String refreshToken = request.getRefreshToken();

            if (!jwtService.isRefreshToken(refreshToken)) {
                throw new AuthException("INVALID_TOKEN", "Invalid refresh token");
            }

            String uuid = jwtService.getUuidFromToken(refreshToken);
            User user = userRepository.findByUuid(uuid)
                    .orElseThrow(() -> new AuthException("USER_NOT_FOUND", "User not found"));

            String newAccessToken = jwtService.generateAccessToken(
                    user.getUuid(),
                    user.getEmail(),
                    user.getRole().name()
            );

            return TokenRefreshResponse.builder()
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getAccessTokenValidity() / 1000)
                    .build();

        } catch (JwtException e) {
            throw new AuthException("INVALID_TOKEN", "Invalid or expired refresh token");
        }
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("EMAIL_EXISTS", "Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .baekjunId(request.getBaekjoonId())
                .build();

        User savedUser = userRepository.save(user);

        return SignupResponse.builder()
                .uuid(savedUser.getUuid())
                .email(savedUser.getEmail())
                .message("User registered successfully")
                .build();
    }
}