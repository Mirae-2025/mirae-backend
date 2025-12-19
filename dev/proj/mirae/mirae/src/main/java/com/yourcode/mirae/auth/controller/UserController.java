package com.yourcode.mirae.auth.controller;

import com.yourcode.mirae.auth.dto.UserProfileResponse;
import com.yourcode.mirae.auth.exception.AuthException;
import com.yourcode.mirae.auth.service.JwtService;
import com.yourcode.mirae.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "User profile management APIs")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @Operation(
            summary = "Get user profile",
            description = "Get current user's profile information",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing token"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @RequestHeader("Authorization") String authHeader) {

        String token = extractToken(authHeader);
        String uuid = jwtService.getUuidFromToken(token);

        UserProfileResponse profile = userService.getProfile(uuid);
        return ResponseEntity.ok(profile);
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthException("INVALID_TOKEN", "Invalid or missing Authorization header");
        }
        return authHeader.substring(7);
    }
}
