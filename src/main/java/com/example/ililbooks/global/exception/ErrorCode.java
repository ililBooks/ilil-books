package com.example.ililbooks.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Getter
public enum ErrorCode {
    // 기본 예외
    AUTHORIZATION(HttpStatus.UNAUTHORIZED, DEFAULT_UNAUTHORIZED),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, DEFAULT_BAD_REQUEST),
    NOT_FOUND(HttpStatus.NOT_FOUND, DEFAULT_NOT_FOUND),
    FORBIDDEN(HttpStatus.FORBIDDEN, DEFAULT_FORBIDDEN),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.INTERNAL_SERVER_ERROR);

    private final HttpStatus httpStatus;
    private final ErrorMessage errorMessage;

    ErrorCode(HttpStatus httpStatus, ErrorMessage errorMessage) {
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }
}
