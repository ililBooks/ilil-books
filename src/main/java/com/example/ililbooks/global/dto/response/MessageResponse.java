package com.example.ililbooks.global.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponse<T> implements Response<T> {
    private final String message;
    private final T data;

    private MessageResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public static <T> MessageResponse<T> of(String message, T data) {
        return new MessageResponse<>(message, data);
    }

    @Override
    public T getData() {
        return data;
    }
}

