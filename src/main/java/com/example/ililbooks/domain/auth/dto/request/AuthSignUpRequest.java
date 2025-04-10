package com.example.ililbooks.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import static com.example.ililbooks.global.dto.ValidationMessage.*;

public record AuthSignUpRequest(@NotBlank(message = NOT_BLANK_EMAIL) @Email(message = PATTERN_EMAIL) String email,
                                @NotBlank(message = NOT_BLANK_NICKNAME) String nickname,
                                @NotBlank(message = NOT_BLANK_PASSWORD) @Pattern(regexp = PATTERN_PASSWORD_REGEXP, message = PATTERN_PASSWORD) String password,
                                String passwordCheck,
                                String userRole) {

    @Builder
    public AuthSignUpRequest(String email, String nickname, String password, String passwordCheck, String userRole) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.passwordCheck = passwordCheck;
        this.userRole = userRole;
    }
}
