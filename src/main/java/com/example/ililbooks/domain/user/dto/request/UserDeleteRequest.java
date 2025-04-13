package com.example.ililbooks.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import static com.example.ililbooks.global.dto.ValidationMessage.NOT_BLANK_PASSWORD;

@Schema(description = "유저 삭제를 요청하기 위한 DTO")
public record UserDeleteRequest(

        @Schema(example = "password1234")
        @NotBlank(message = NOT_BLANK_PASSWORD)
        String password
) {
}