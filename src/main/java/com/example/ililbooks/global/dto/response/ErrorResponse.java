package com.example.ililbooks.global.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse<T> {
    private final HttpStatus code;
    private final T message;
    private final Object data;

    private ErrorResponse(HttpStatus code, T message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ErrorResponse<T> of(HttpStatus code, T message) {
        return new ErrorResponse<>(code, message, null);
    }

    public static <T> ErrorResponse<T> of(HttpStatus code, T message, Object data) {
        return new ErrorResponse<>(code, message, data);
    }
}
