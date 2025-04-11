package com.example.ililbooks.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

import static com.example.ililbooks.global.dto.ValidationMessage.NOT_BLANK_PASSWORD;

public record UserDeleteRequest(
        @NotBlank(message = NOT_BLANK_PASSWORD)
        String password
) {
}