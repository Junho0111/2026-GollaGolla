package com.gollagolla.auth.ui.dto;

import lombok.Getter;

@Getter
public class LoginResponse {

    private String accessToken;
    private String refreshToken;

    private LoginResponse() {
    }

    private LoginResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static LoginResponse of(String accessToken, String refreshToken) {
        return new LoginResponse(accessToken, refreshToken);
    }
}