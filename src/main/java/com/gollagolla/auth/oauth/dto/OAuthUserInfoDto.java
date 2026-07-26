package com.gollagolla.auth.oauth.dto;

import lombok.Getter;

@Getter
public class OAuthUserInfoDto {

    private String providerId;
    private String email;
    private String nickname;

    private OAuthUserInfoDto() {
    }

    public OAuthUserInfoDto(String providerId, String email, String nickname) {
        this.providerId = providerId;
        this.email = email;
        this.nickname = nickname;
    }
}
