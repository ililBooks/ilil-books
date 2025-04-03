package com.example.ililbooks.global.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponse {
    String message;

    private MessageResponse(String message) {
        this.message = message;
    }

    public static MessageResponse of(String message) {
        return new MessageResponse(message);
    }
}
