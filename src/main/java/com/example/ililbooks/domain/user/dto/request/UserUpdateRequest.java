package com.example.ililbooks.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import static com.example.ililbooks.global.dto.ValidationMessage.*;

@Schema(description = "유저 정보 수정을 요청하기 위한 DTO")
public record UserUpdateRequest(

        @Schema(example = "닉네임")
        @NotBlank(message = NOT_BLANK_NICKNAME)
        String nickname,

        @Schema(example = "12345")
        @NotBlank(message = NOT_BLANK_ZIPCODE)
        @Pattern(regexp = PATTERN_ZIPCODE_REGEXP, message = PATTERN_ZIPCODE)
        String zipCode,

        @Schema(example = "도로명주소")
        @NotBlank(message = NOT_BLANK_ROAD_ADDRESS)
        String roadAddress,

        @Schema(example = "111호 222동")
        @NotBlank(message = NOT_BLANK_DETAILED_ADDRESS)
        String detailedAddress,

        @Schema(example = "01012345678")
        @NotBlank(message = NOT_BLANK_CONTACT_NUMBER)
        @Size(min = 10, max = 11, message = VALIDATE_CONTACT_NUMBER_SIZE)
        String contactNumber
) {

    @Builder
    public UserUpdateRequest(String nickname, String zipCode, String roadAddress, String detailedAddress, String contactNumber) {
        this.nickname = nickname;
        this.zipCode = zipCode;
        this.roadAddress = roadAddress;
        this.detailedAddress = detailedAddress;
        this.contactNumber = contactNumber;
    }
}
