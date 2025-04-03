package com.example.ililbooks.global.exception;

import com.example.ililbooks.global.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.example.ililbooks.global.exception.ErrorMessage.INTERNAL_SERVER_ERROR;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ErrorResponse<String> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getFieldErrors().stream().
                findFirst().
                map(DefaultMessageSourceResolvable::getDefaultMessage).
                orElseThrow(() -> new IllegalStateException("검증 에러가 반드시 존재해야 합니다."));
        return ErrorResponse.of(HttpStatus.BAD_REQUEST, errorMessage);
    }

    @ExceptionHandler(HandledException.class)
    public ResponseEntity<ErrorResponse<String>> handleCustomException(HandledException e) {
        log.info("CustomException : {}", e.getMessage(), e);
        return new ResponseEntity<>(ErrorResponse.of(e.getHttpStatus(), e.getMessage()), e.getHttpStatus());
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse<String> handleGlobalException(Exception e) {
        log.error("Exception : {}",e.getMessage(),  e);
        return ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR.getMessage());
    }
}
