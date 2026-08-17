package com.gollagolla.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH_001", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "AUTH_002", "이미 사용 중인 닉네임입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_003", "이메일 또는 비밀번호가 올바르지 않습니다."),
    SOCIAL_LOGIN_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH_004", "소셜 계정으로 가입된 이메일입니다. OAuth 로그인을 이용해 주세요."),
    ALREADY_REGISTERED_EMAIL(HttpStatus.CONFLICT, "AUTH_005", "이미 다른 방식으로 가입된 이메일입니다."),
    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_006", "지원하지 않는 OAuth Provider입니다."),
    MISSING_BEARER_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_007", "Bearer 토큰이 존재하지 않습니다."),
    EMPTY_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_008", "OAuth Provider 이름이 비어있습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_009", "유효하지 않은 토큰입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_010", "존재하지 않는 회원입니다."),

    POI_NOT_FOUND(HttpStatus.NOT_FOUND, "POI_001", "존재하지 않는 장소입니다."),
    UNSUPPORTED_SEARCH_TYPE(HttpStatus.BAD_REQUEST, "POI_002", "지원하지 않는 검색 타입입니다. 현재 지원: poi"),

    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "REVIEW_001", "이미 리뷰를 작성한 장소입니다."),
    REVIEW_INVALID_RATING(HttpStatus.BAD_REQUEST, "REVIEW_002", "평점은 1~5 사이여야 합니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_003", "존재하지 않는 리뷰입니다."),

    INVALID_TRAVEL_PERIOD(HttpStatus.BAD_REQUEST, "DOMAIN_001", "시작일은 종료일보다 빨라야 합니다."),
    INVALID_REGION_HIERARCHY(HttpStatus.BAD_REQUEST, "DOMAIN_002", "지역 계층 설정이 올바르지 않습니다."),
    INVALID_RATING_VALUE(HttpStatus.BAD_REQUEST, "DOMAIN_003", "평점은 1~5 사이여야 합니다."),

    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "SYS_001", "아직 구현되지 않은 기능입니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 입력 값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_500", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String clientMessage;

    ErrorCode(HttpStatus httpStatus, String code, String clientMessage) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.clientMessage = clientMessage;
    }
}
