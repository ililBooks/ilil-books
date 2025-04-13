package com.example.ililbooks.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import static com.example.ililbooks.global.dto.ValidationMessage.*;

@Schema(description = "이메일을 사용한 회원가입을 위한 DTO")
public record AuthSignUpRequest(

        @Schema(example = "example@example.com")
        @NotBlank(message = NOT_BLANK_EMAIL)
        @Email(message = PATTERN_EMAIL)
        String email,

        @Schema(example = "닉네임")
        @NotBlank(message = NOT_BLANK_NICKNAME)
        String nickname,

        @Schema(example = "password1234")
        @NotBlank(message = NOT_BLANK_PASSWORD)
        @Pattern(regexp = PATTERN_PASSWORD_REGEXP, message = PATTERN_PASSWORD)
        String password,

        @Schema(example = "password1234")
        String passwordCheck,

        @Schema(example = "ROLE_USER")
        String userRole
) {

    @Builder
    public AuthSignUpRequest(String email, String nickname, String password, String passwordCheck, String userRole) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.passwordCheck = passwordCheck;
        this.userRole = userRole;
    }
}
