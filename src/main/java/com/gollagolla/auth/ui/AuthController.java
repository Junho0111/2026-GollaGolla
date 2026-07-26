package com.gollagolla.auth.ui;

import com.gollagolla.auth.application.AuthService;
import com.gollagolla.auth.ui.dto.*;
import com.gollagolla.member.domain.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignUpResponse signup(@RequestBody SignUpRequest request) {
        return authService.signUp(request.getEmail(), request.getPassword(), request.getNickname());
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request.getEmail(), request.getPassword());
    }

    @PostMapping("/oauth/{provider}")
    public OAuthResponse oauthLogin(@PathVariable("provider") String providerName, @RequestBody OAuthRequest request) {
        Provider provider = Provider.from(providerName);
        return authService.oauthLogin(provider, request.getAuthorizationCode());
    }
}
