package com.gollagolla.auth.oauth;

import com.gollagolla.auth.oauth.dto.OAuthUserInfoDto;
import com.gollagolla.member.domain.Provider;
import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class KakaoOAuthClient implements OAuthClient {

    private final RestClient restClient;
    private final String clientId;
    private final String redirectUri;

    public KakaoOAuthClient(RestClient restClient,
                            @Value("${oauth.kakao.client-id}") String clientId,
                            @Value("${oauth.kakao.redirect-uri}") String redirectUri
    ) {
        this.restClient = restClient;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }

    @Override
    public Provider provider() {
        return Provider.KAKAO;
    }

    @Override
    public OAuthUserInfoDto getUserInfo(String authorizationCode) {
        String accessToken = getAccessToken(authorizationCode);
        return fetchUserInfo(accessToken);
    }

    private String getAccessToken(String authorizationCode) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", authorizationCode);

        KakaoTokenResponse response = restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(KakaoTokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "카카오 액세스 토큰을 가져올 수 없습니다.");
        }
        return response.accessToken();
    }

    private OAuthUserInfoDto fetchUserInfo(String accessToken) {
        KakaoUserResponse kakaoUserResponse = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(KakaoUserResponse.class);

        if (kakaoUserResponse == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "카카오 유저 정보를 가져올 수 없습니다.");
        }

        String providerId = String.valueOf(kakaoUserResponse.id());
        String email = kakaoUserResponse.kakaoAccount().email();
        String nickname = kakaoUserResponse.kakaoAccount().profile().nickname();

        return new OAuthUserInfoDto(providerId, email, nickname);
    }

    private record KakaoTokenResponse(@JsonProperty("access_token") String accessToken) {}

    private record KakaoUserResponse(Long id, @JsonProperty("kakao_account") KakaoAccount kakaoAccount) {
        public record KakaoAccount(String email, Profile profile) {
            public record Profile(String nickname) {}
        }
    }
}
