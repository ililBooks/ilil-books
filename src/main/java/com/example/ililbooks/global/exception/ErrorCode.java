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

    // 내부 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.INTERNAL_SERVER_ERROR),

    // Redisson 락 관련
    LOCK_ACQUISITION_FAILED(HttpStatus.CONFLICT, REDISSON_LOCK_FAILED),
    LOCK_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR, REDISSON_LOCK_INTERRUPTED),

    // Redis (직렬화/역직렬화)
    REDIS_PARSING_FAILED(HttpStatus.BAD_REQUEST, ErrorMessage.REDIS_PARSING_FAILED),
    REDIS_SERIALIZE_FAILED(HttpStatus.BAD_REQUEST, ErrorMessage.REDIS_SERIALIZE_FAILED),

    // 예약 관련
    RESERVATION_DUPLICATE(HttpStatus.BAD_REQUEST, RESERVATION_ALREADY_EXISTS),
    NOT_FOUND_RESERVATION(HttpStatus.NOT_FOUND, ErrorMessage.NOT_FOUND_RESERVATION),
    RESERVATION_EXPIRED(HttpStatus.BAD_REQUEST, ErrorMessage.RESERVATION_EXPIRED),
    RESERVATION_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, ErrorMessage.RESERVATION_ALREADY_CANCELED),

    // 행사 관련
    NOT_FOUND_EVENT(HttpStatus.NOT_FOUND, ErrorMessage.NOT_FOUND_EVENT),
    EVENT_ALREADY_ENDED(HttpStatus.BAD_REQUEST, ErrorMessage.EVENT_ALREADY_ENDED),
    EVENT_DELETED(HttpStatus.BAD_REQUEST, ErrorMessage.EVENT_DELETED),

    // 주문 관련
    ORDER_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.ORDER_CREATION_FAILED),
    ORDER_ALREADY_CREATED(HttpStatus.BAD_REQUEST, ORDER_ALREADY_EXISTS);


    private final HttpStatus httpStatus;
    private final ErrorMessage errorMessage;

    ErrorCode(HttpStatus httpStatus, ErrorMessage errorMessage) {
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }
}
