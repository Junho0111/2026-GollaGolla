package com.gollagolla.auth.ui.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class OAuthRequest {

    @NotBlank(message = "인가 코드는 필수입니다.")
    private String authorizationCode;

    private OAuthRequest() {
    }

    public OAuthRequest(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }
}