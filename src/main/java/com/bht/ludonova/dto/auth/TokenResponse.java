package com.bht.ludonova.dto.auth;

import lombok.Data;

@Data
public class TokenResponse {
    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final long expiresIn;

    public static TokenResponse of(String accessToken, String refreshToken, long expiresIn) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", expiresIn);
    }
}