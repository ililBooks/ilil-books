package com.example.ililbooks.global.dto.response;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public interface Response<T> {

    T getData();

    @ResponseStatus(value = HttpStatus.OK)
    static <T> Response<T> of(T data) {
        return new DefaultResponse<>(data);
    }

    @ResponseStatus(value = HttpStatus.CREATED)
    static <T> Response<T> created(T data) {
        return new DefaultResponse<>(data);
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    static <T> Response<T> empty() {
        return new DefaultResponse<>(null);
    }
}
