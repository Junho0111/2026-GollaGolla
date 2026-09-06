package com.gollagolla.auth.ui;

import com.gollagolla.auth.support.JwtTokenProvider;
import com.gollagolla.global.exception.ErrorCode;
import tools.jackson.databind.ObjectMapper;
import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.auth.application.AuthService;
import com.gollagolla.auth.ui.dto.LoginResponse;
import com.gollagolla.auth.ui.dto.OAuthResponse;
import com.gollagolla.auth.ui.dto.SignUpResponse;
import com.gollagolla.member.domain.Provider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @MockitoBean
    AuthService authService;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ──────────────── 회원가입 ────────────────

    @Test
    void 회원가입_요청_시_201과_토큰_반환() throws Exception {
        // given
        Map<String, String> body = Map.of(
                "email", "test@test.com",
                "password", "testPassword",
                "nickname", "테스터"
        );

        SignUpResponse response = new SignUpResponse(1L, "access-token", "refresh-token");
        given(authService.signUp("test@test.com", "testPassword", "테스터")).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void 회원가입_시_중복_이메일이면_409_반환() throws Exception {
        // given
        Map<String, String> body = Map.of(
                "email", "중복@test.com",
                "password", "testPassword",
                "nickname", "테스터"
        );

        given(authService.signUp("중복@test.com", "testPassword", "테스터"))
                .willThrow(new BusinessException(ErrorCode.DUPLICATE_EMAIL));

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("AUTH_001"))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }

    // ──────────────── 로그인 ────────────────

    @Test
    void 로그인_요청_시_200과_토큰_반환() throws Exception {
        // given
        Map<String, String> body = Map.of(
                "email", "test@test.com",
                "password", "testPassword"
        );

        LoginResponse response = LoginResponse.of("access-token", "refresh-token");
        given(authService.login("test@test.com", "testPassword")).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void 로그인_시_비밀번호_틀리면_401_반환() throws Exception {
        // given
        Map<String, String> body = Map.of(
                "email", "test@test.com",
                "password", "wrongPassword"
        );

        given(authService.login("test@test.com", "wrongPassword"))
                .willThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_003"))
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    void OAuth_로그인_요청_시_200과_응답_반환() throws Exception {
        // given
        Map<String, String> body = Map.of("authorizationCode", "auth-code-123");
        OAuthResponse response = new OAuthResponse("access-token", "refresh-token", true);
        given(authService.oauthLogin(Provider.KAKAO, "auth-code-123")).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/auth/oauth/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    void 미지원_OAuth_provider_요청_시_400_반환() throws Exception {
        // given
        Map<String, String> body = Map.of("authorizationCode", "auth-code");

        // when & then
        mockMvc.perform(post("/api/v1/auth/oauth/invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH_006"));
    }

    @Test
    void 인증된_사용자가_로그아웃_요청_시_204_반환() throws Exception {
        // given
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, List.of()));
        willDoNothing().given(authService).logout(1L);

        // when & then
        mockMvc.perform(delete("/api/v1/auth/logout"))
                .andExpect(status().isNoContent());

        then(authService).should().logout(1L);
    }

    @Test
    void 인증된_사용자가_회원탈퇴_요청_시_204_반환() throws Exception {
        // given
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, List.of()));
        willDoNothing().given(authService).withdraw(1L);

        // when & then
        mockMvc.perform(delete("/api/v1/auth/withdraw"))
                .andExpect(status().isNoContent());

        then(authService).should().withdraw(1L);
    }

    @Test
    void 회원탈퇴_시_존재하지_않는_회원이면_404_반환() throws Exception {
        // given
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, List.of()));
        willThrow(new BusinessException(ErrorCode.MEMBER_NOT_FOUND))
                .given(authService).withdraw(1L);

        // when & then
        mockMvc.perform(delete("/api/v1/auth/withdraw"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("AUTH_010"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 회원입니다."));
    }
}
