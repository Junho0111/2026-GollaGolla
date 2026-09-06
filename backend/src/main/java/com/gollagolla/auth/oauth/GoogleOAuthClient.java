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
public class GoogleOAuthClient implements OAuthClient {

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public GoogleOAuthClient(RestClient restClient,
                             @Value("${oauth.google.client-id}") String clientId,
                             @Value("${oauth.google.client-secret}") String clientSecret,
                             @Value("${oauth.google.redirect-uri}") String redirectUri
    ) {
        this.restClient = restClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    @Override
    public Provider provider() {
        return Provider.GOOGLE;
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
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", authorizationCode);

        GoogleTokenResponse response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(GoogleTokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "구글 액세스 토큰을 가져올 수 없습니다.");
        }
        return response.accessToken();
    }

    private OAuthUserInfoDto fetchUserInfo(String accessToken) {
        GoogleUserResponse googleUserResponse = restClient.get()
                .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(GoogleUserResponse.class);

        if (googleUserResponse == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "구글 유저 정보를 가져올 수 없습니다.");
        }

        String providerId = googleUserResponse.id();
        String email = googleUserResponse.email();
        String nickname = googleUserResponse.name();

        return new OAuthUserInfoDto(providerId, email, nickname);
    }

    private record GoogleTokenResponse(@JsonProperty("access_token") String accessToken) {}

    private record GoogleUserResponse(String id, String email, String name) {}
}
