package com.gollagolla.auth.ui.dto;

import lombok.Getter;

@Getter
public class OAuthRequest {

    private String authorizationCode;

    private OAuthRequest() {
    }

    public OAuthRequest(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }
}