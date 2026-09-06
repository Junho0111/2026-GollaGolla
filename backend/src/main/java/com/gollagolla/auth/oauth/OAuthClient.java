package com.gollagolla.auth.oauth;

import com.gollagolla.auth.oauth.dto.OAuthUserInfoDto;
import com.gollagolla.member.domain.Provider;

public interface OAuthClient {

    Provider provider();

    OAuthUserInfoDto getUserInfo(String authorizationCode);
}
