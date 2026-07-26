package com.gollagolla.auth.ui.dto;

import lombok.Getter;

@Getter
public class LoginRequest {

    private String email;
    private String password;

    private LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}