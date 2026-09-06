package com.gollagolla.auth.ui.dto;

import lombok.Getter;

@Getter
public class SignUpResponse {

    private Long memberId;
    private String accessToken;
    private String refreshToken;

    private SignUpResponse() {
    }

    public SignUpResponse(Long memberId, String accessToken, String refreshToken) {
        this.memberId = memberId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}