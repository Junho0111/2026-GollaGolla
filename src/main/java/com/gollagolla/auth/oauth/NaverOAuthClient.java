package com.gollagolla.auth.oauth;

import com.gollagolla.auth.oauth.dto.OAuthUserInfoDto;
import com.gollagolla.member.domain.Provider;
import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class NaverOAuthClient implements OAuthClient {

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;

    public NaverOAuthClient(RestClient restClient,
                            @Value("${oauth.naver.client-id}") String clientId,
                            @Value("${oauth.naver.client-secret}") String clientSecret
    ) {
        this.restClient = restClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public Provider provider() {
        return Provider.NAVER;
    }

    @Override
    public OAuthUserInfoDto getUserInfo(String authorizationCode) {
        String accessToken = getAccessToken(authorizationCode);
        return fetchUserInfo(accessToken);
    }

    private String getAccessToken(String authorizationCode) {
        String state = "gollagolla-state";

        NaverTokenResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https").host("nid.naver.com").path("/oauth2.0/token")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("code", authorizationCode)
                        .queryParam("state", state)
                        .build())
                .retrieve()
                .body(NaverTokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "네이버 액세스 토큰을 가져올 수 없습니다.");
        }
        return response.accessToken();
    }

    private OAuthUserInfoDto fetchUserInfo(String accessToken) {
        NaverUserResponse naverUserResponse = restClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(NaverUserResponse.class);

        if (naverUserResponse == null || naverUserResponse.response() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "네이버 유저 정보를 가져올 수 없습니다.");
        }

        String providerId = naverUserResponse.response().id();
        String email = naverUserResponse.response().email();
        String nickname = naverUserResponse.response().nickname();

        return new OAuthUserInfoDto(providerId, email, nickname);
    }

    private record NaverTokenResponse(@JsonProperty("access_token") String accessToken) {}

    private record NaverUserResponse(Response response) {
        public record Response(String id, String email, String nickname) {}
    }
}
