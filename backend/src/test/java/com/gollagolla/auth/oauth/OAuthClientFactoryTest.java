package com.gollagolla.auth.oauth;

import com.gollagolla.auth.oauth.dto.OAuthUserInfoDto;
import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.member.domain.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class OAuthClientFactoryTest {

    private OAuthClientFactory oAuthClientFactory;

    @BeforeEach
    void setUp() {
        OAuthClient kakaoClient = new OAuthClient() {
            @Override
            public Provider provider() { return Provider.KAKAO; }
            @Override
            public OAuthUserInfoDto getUserInfo(String code) {
                return new OAuthUserInfoDto("kakao-id", "kakao@test.com", "카카오유저");
            }
        };

        OAuthClient naverClient = new OAuthClient() {
            @Override
            public Provider provider() { return Provider.NAVER; }
            @Override
            public OAuthUserInfoDto getUserInfo(String code) {
                return new OAuthUserInfoDto("naver-id", "naver@test.com", "네이버유저");
            }
        };

        oAuthClientFactory = new OAuthClientFactory(List.of(kakaoClient, naverClient));
    }

    @Test
    void KAKAO_provider로_카카오_클라이언트_반환() throws Exception {
        // given
        Provider provider = Provider.KAKAO;

        // when
        OAuthClient client = oAuthClientFactory.getClient(provider);

        // then
        assertThat(client.provider()).isEqualTo(Provider.KAKAO);
    }

    @Test
    void NAVER_provider로_네이버_클라이언트_반환() throws Exception {
        // given
        Provider provider = Provider.NAVER;

        // when
        OAuthClient client = oAuthClientFactory.getClient(provider);

        // then
        assertThat(client.provider()).isEqualTo(Provider.NAVER);
    }

    @Test
    void 등록되지않은_provider_조회_시_예외_발생() throws Exception {
        // given
        Provider provider = Provider.GOOGLE; // 등록 안 된 provider

        // when & then
        assertThatThrownBy(() -> oAuthClientFactory.getClient(provider))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("지원하지 않는 OAuth Provider입니다");
    }

    @Test
    void 클라이언트_getUserInfo_정상_반환() throws Exception {
        // given
        OAuthClient client = oAuthClientFactory.getClient(Provider.KAKAO);

        // when
        OAuthUserInfoDto userInfo = client.getUserInfo("any-code");

        // then
        assertThat(userInfo.getProviderId()).isEqualTo("kakao-id");
        assertThat(userInfo.getEmail()).isEqualTo("kakao@test.com");
        assertThat(userInfo.getNickname()).isEqualTo("카카오유저");
    }
}
