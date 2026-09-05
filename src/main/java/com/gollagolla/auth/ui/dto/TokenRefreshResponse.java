package com.gollagolla.auth.ui.dto;

import lombok.Getter;

@Getter
public class TokenRefreshResponse {

    private String accessToken;
    private String refreshToken;

    private TokenRefreshResponse() {}

    private TokenRefreshResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static TokenRefreshResponse of(String accessToken, String refreshToken) {
        return new TokenRefreshResponse(accessToken, refreshToken);
    }
}
