package com.gollagolla.auth.oauth;

import com.gollagolla.member.domain.Provider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OAuthClientFactory {

    private final Map<Provider, OAuthClient> clients;

    public OAuthClientFactory(List<OAuthClient> clientList) {
        this.clients = clientList.stream()
                .collect(Collectors.toMap(
                        OAuthClient -> OAuthClient.provider(),
                        OAuthClient -> OAuthClient
                ));
    }

    public OAuthClient getClient(Provider provider) {
        OAuthClient client = clients.get(provider);
        if (client == null) {
            throw new IllegalArgumentException("지원하지 않는 OAuth Provider입니다: " + provider);
        }
        return client;
    }
}
