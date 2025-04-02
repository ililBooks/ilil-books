package com.example.ililbooks.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;
    private final String message;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.status = errorCode.getStatus();
        this.errorCode = errorCode.getCode();
        this.message = errorCode.getDefaultMessage();
    }

    public CustomException(ErrorCode errorCode, String message) {
        super(message);
        this.status = errorCode.getStatus();
        this.errorCode = errorCode.getCode();
        this.message = message;
    }
}
