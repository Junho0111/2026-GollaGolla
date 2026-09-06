package com.gollagolla.auth.support;

import jakarta.servlet.http.HttpServletRequest;
import com.gollagolla.global.exception.BusinessException;
import com.gollagolla.global.exception.ErrorCode;

public class AuthorizationExtractor {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX  = "Bearer ";

    private AuthorizationExtractor() {
    }

    public static String extract(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length()).trim();
        }
        throw new BusinessException(ErrorCode.MISSING_BEARER_TOKEN);
    }
}
