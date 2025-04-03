package com.example.ililbooks.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class HandledException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String message;

    // 예외 던질시 기본 메세지 출력
    public HandledException(ErrorCode errorCode) {
        super(errorCode.getErrorMessage().getMessage());
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getErrorMessage().getMessage();
    }

    // 예외 던질시 메세지 출력
    public HandledException(ErrorCode errorCode, String message) {
        super(message);
        this.httpStatus = errorCode.getHttpStatus();
        this.message = message;
    }
}