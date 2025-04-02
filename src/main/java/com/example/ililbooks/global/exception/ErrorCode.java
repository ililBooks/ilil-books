package com.example.ililbooks.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    AUTHORIZATION(HttpStatus.UNAUTHORIZED,"AUTHORIZATION","인증이 필요합니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST,"BAD_REQUEST","잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND,"NOT_FOUND","찾지 못했습니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN,"FORBIDDEN","권한이 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;


    ErrorCode(HttpStatus status, String code, String defaultMessage) {
        this.status = status;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
