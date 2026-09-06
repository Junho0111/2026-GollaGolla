package com.gollagolla.auth.ui.dto;

import lombok.Getter;

@Getter
public class OAuthResponse {

    private String accessToken;
    private String refreshToken;
    private boolean isNewUser;


    private OAuthResponse() {
    }

    public OAuthResponse(String accessToken, String refreshToken, boolean isNewUser) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.isNewUser = isNewUser;
    }
}