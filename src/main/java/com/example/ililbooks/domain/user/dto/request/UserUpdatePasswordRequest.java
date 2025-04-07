package com.example.ililbooks.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

import static com.example.ililbooks.global.dto.ValidationMessage.*;

@Getter
public class UserUpdatePasswordRequest {

    @NotBlank(message = NOT_BLANK_PASSWORD)
    private String oldPassword;

    @NotBlank(message = NOT_BLANK_PASSWORD)
    @Pattern(regexp = PATTERN_PASSWORD_REGEXP,
            message = PATTERN_PASSWORD)
    private String newPassword;

    @NotBlank(message = NOT_BLANK_PASSWORD)
    private String newPasswordCheck;

    @Builder
    private UserUpdatePasswordRequest(String oldPassword, String newPassword, String newPasswordCheck) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.newPasswordCheck = newPasswordCheck;
    }
}
