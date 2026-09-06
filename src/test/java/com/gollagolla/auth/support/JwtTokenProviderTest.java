package com.gollagolla.auth.support;

import com.gollagolla.member.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String SECRET = "gollagolla-secret-test-key-must-be-at-least-32-characters-long";
    private static final long ACCESS_EXPIRY_MS  = 3600000;
    private static final long REFRESH_EXPIRY_MS = 604800000;

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void beforeEach() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, ACCESS_EXPIRY_MS, REFRESH_EXPIRY_MS);
    }

    @Test
    void 액세스_토큰_발급_후_검증_성공() throws Exception {
        // given & when
        String token = jwtTokenProvider.generateAccessToken(1L, Role.USER);
        Claims claims = jwtTokenProvider.validateToken(token);

        // then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("1");
    }

    @Test
    void 리프레시_토큰_발급_후_검증_성공() throws Exception {
        // given & when
        String token  = jwtTokenProvider.generateRefreshToken(1L);
        Claims claims = jwtTokenProvider.validateToken(token);

        // then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("1");
    }

    @Test
    void 토큰에서_memberId_정상_추출() throws Exception {
        // given
        String token  = jwtTokenProvider.generateAccessToken(1L, Role.USER);
        Claims claims = jwtTokenProvider.validateToken(token);

        // when
        Long extractId = jwtTokenProvider.extractMemberId(claims);

        // then
        assertThat(extractId).isEqualTo(1L);
    }

    @Test
    void 만료된_토큰_검증_시_JwtException_발생() throws Exception {
        // given
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 1L, 1L); // 1ms 후 만료
        String token = provider.generateAccessToken(1L, Role.USER);
        Thread.sleep(2); // 만료 대기

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void 다른_시크릿으로_서명된_토큰_검증_시_JwtException_발생() throws Exception {
        // given
        JwtTokenProvider otherProvider = new JwtTokenProvider(
                "other-gollagolla-secret-test-key-must-be-at-least-32-characters-long", ACCESS_EXPIRY_MS, REFRESH_EXPIRY_MS);
        String token = otherProvider.generateAccessToken(1L, Role.USER);

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void 형식이_잘못된_문자열_검증_시_JwtException_발생() throws Exception {
        // given
        String invalidToken = "test.Header.Payload.Signature";

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(invalidToken))
                .isInstanceOf(JwtException.class);
    }
}
