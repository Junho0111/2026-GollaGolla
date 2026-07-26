package com.gollagolla.auth.ui.dto;

import lombok.Getter;

@Getter
public class SignUpRequest {

    private String email;
    private String password;
    private String nickname;

    private SignUpRequest() {
    }

    public SignUpRequest(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }
}