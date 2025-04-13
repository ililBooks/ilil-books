package com.example.ililbooks.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

import static com.example.ililbooks.global.dto.ValidationMessage.*;

@Schema(description = "유저 비밀번호 수정을 요청하기 위한 DTO")
public record UserUpdatePasswordRequest(

        @Schema(example = "oldPassword1234")
        @NotBlank(message = NOT_BLANK_PASSWORD)
        String oldPassword,

        @Schema(example = "newPassword1234")
        @NotBlank(message = NOT_BLANK_PASSWORD)
        @Pattern(regexp = PATTERN_PASSWORD_REGEXP, message = PATTERN_PASSWORD)
        String newPassword,

        @Schema(example = "newPassword1234")
        @NotBlank(message = NOT_BLANK_PASSWORD)
        String newPasswordCheck
) {

    @Builder
    public UserUpdatePasswordRequest(String oldPassword, String newPassword, String newPasswordCheck) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.newPasswordCheck = newPasswordCheck;
    }
}
