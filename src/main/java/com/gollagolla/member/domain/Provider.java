package com.gollagolla.member.domain;

import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.global.exception.ErrorCode;

public enum Provider {
    LOCAL, KAKAO, NAVER, GOOGLE;

    public static Provider from(String providerName) {
        if (providerName == null || providerName.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_OAUTH_PROVIDER);
        }

        try {
            return Provider.valueOf(providerName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER, "지원하지 않는 OAuth Provider입니다: " + providerName);
        }
    }
}
