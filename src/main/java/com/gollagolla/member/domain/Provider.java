package com.gollagolla.member.domain;

public enum Provider {
    LOCAL, KAKAO, NAVER, GOOGLE;

    public static Provider from(String provider) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("OAuth Provider 이름이 비어있습니다.");
        }

        try {
            return Provider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 OAuth Provider입니다: " + provider);
        }
    }
}
