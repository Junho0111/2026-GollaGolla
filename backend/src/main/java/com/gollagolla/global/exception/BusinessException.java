package com.gollagolla.global.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String serverMessage;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getClientMessage());
        this.errorCode = errorCode;
        this.serverMessage = errorCode.getClientMessage();
    }

    public BusinessException(ErrorCode errorCode, String serverMessage) {
        super(errorCode.getClientMessage());
        this.errorCode = errorCode;
        this.serverMessage = serverMessage;
    }
}
