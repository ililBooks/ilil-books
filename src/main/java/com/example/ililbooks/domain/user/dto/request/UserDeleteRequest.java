package com.example.ililbooks.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

import static com.example.ililbooks.global.dto.ValidationMessage.NOT_BLANK_PASSWORD;

@Getter
public class UserDeleteRequest {

    @NotBlank(message = NOT_BLANK_PASSWORD)
    private String Password;

    @Builder
    private UserDeleteRequest(String password) {
        this.Password = password;
    }
}
