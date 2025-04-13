package com.example.ililbooks.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "이메일을 사용한 로그인을 위한 DTO")
public record AuthSignInRequest(

        @Schema(example = "example@example.com")
        String email,

        @Schema(example = "password1234")
        String password
) {

    @Builder
    public AuthSignInRequest {
    }
}
