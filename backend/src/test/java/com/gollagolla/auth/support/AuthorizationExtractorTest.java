package com.gollagolla.auth.support;

import org.junit.jupiter.api.Test;
import com.gollagolla.global.exception.BusinessException;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.*;

class AuthorizationExtractorTest {

    @Test
    void Bearer_토큰_정상_추출() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.payload.signature");

        // when
        String token = AuthorizationExtractor.extract(request);

        // then
        assertThat(token).isEqualTo("eyJhbGciOiJIUzI1NiJ9.payload.signature");
    }

    @Test
    void Authorization_헤더가_없으면_예외_발생() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // when & then
        assertThatThrownBy(() -> AuthorizationExtractor.extract(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Bearer 토큰이 존재하지 않습니다.");
    }

    @Test
    void Bearer_prefix_없는_헤더이면_예외_발생() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "none Bearer eyJhbGciOiJIUzI1NiJ9.payload.signature");

        // when & then
        assertThatThrownBy(() -> AuthorizationExtractor.extract(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Bearer 토큰이 존재하지 않습니다.");
    }

    @Test
    void 토큰_앞뒤_공백_제거() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer       eyJhbGciOiJIUzI1NiJ9.payload.signature       ");

        // when
        String token = AuthorizationExtractor.extract(request);

        // then
        assertThat(token).isEqualTo("eyJhbGciOiJIUzI1NiJ9.payload.signature");
    }
}
