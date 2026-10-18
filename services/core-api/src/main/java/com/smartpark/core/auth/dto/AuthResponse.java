package com.smartpark.core.auth.dto;

import java.util.Set;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresInMs,
    UserProfileDto user
) {
    public static AuthResponse of(String accessToken, String refreshToken, long expiresInMs, UserProfileDto user) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresInMs, user);
    }
}
