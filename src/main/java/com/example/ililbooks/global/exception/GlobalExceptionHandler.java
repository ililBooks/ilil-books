package com.example.ililbooks.global.exception;

import com.example.ililbooks.global.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ErrorResponse handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getFieldErrors().stream().
                findFirst().
                map(DefaultMessageSourceResolvable::getDefaultMessage).
                orElseThrow(() -> new IllegalStateException("검증 에러가 반드시 존재해야 합니다."));
        return ErrorResponse.of("VALIDATION_ERROR", errorMessage);
    }

    @ResponseStatus(value = HttpStatus.CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ErrorResponse handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return ErrorResponse.of("UNIQUE_ERROR", "존재하는 데이터입니다.");
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.info("CustomException : {}", e.getMessage(), e);
        return new ResponseEntity<>(ErrorResponse.of(e.getErrorCode(), e.getMessage()), e.getStatus());
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleGlobalException(Exception e) {
        log.error("Exception : {}",e.getMessage(),  e);
        return ErrorResponse.of("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");
    }
}
