package com.example.ililbooks.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

import static com.example.ililbooks.global.dto.ValidationMessage.*;

@Getter
public class AuthSignUpRequest {

    @NotBlank(message = NOT_BLANK_EMAIL)
    @Email(message = PATTERN_EMAIL)
    private String email;

    @NotBlank(message = NOT_BLANK_NICKNAME)
    private String nickname;

    @NotBlank(message = NOT_BLANK_PASSWORD)
    @Pattern(regexp = PATTERN_PASSWORD_REGEXP,
            message = PATTERN_PASSWORD)
    private String password;

    private String passwordCheck;

    private String userRole;

    @Builder
    private AuthSignUpRequest(String email, String nickname, String password, String passwordCheck, String userRole) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.passwordCheck = passwordCheck;
        this.userRole = userRole;
    }
}
