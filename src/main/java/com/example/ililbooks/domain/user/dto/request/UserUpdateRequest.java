package com.example.ililbooks.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import static com.example.ililbooks.global.dto.ValidationMessage.*;

@Getter
public class UserUpdateRequest {

    @NotBlank(message = NOT_BLANK_NICKNAME)
    private String nickname;

    @NotBlank(message = NOT_BLANK_ZIPCODE)
    @Pattern(regexp = PATTERN_ZIPCODE_REGEXP,
            message = PATTERN_ZIPCODE)
    private String zipCode;

    @NotBlank(message = NOT_BLANK_ROAD_ADDRESS)
    private String roadAddress;

    @NotBlank(message = NOT_BLANK_DETAILED_ADDRESS)
    private String detailedAddress;

    @NotBlank(message = NOT_BLANK_CONTACT_NUMBER)
    @Size(min = 10, max = 11, message = VALIDATE_CONTACT_NUMBER_SIZE)
    private String contactNumber;

    @Builder
    private UserUpdateRequest(String nickname, String zipCode, String roadAddress, String detailedAddress, String contactNumber) {
        this.nickname = nickname;
        this.zipCode = zipCode;
        this.roadAddress = roadAddress;
        this.detailedAddress = detailedAddress;
        this.contactNumber = contactNumber;
    }
}
