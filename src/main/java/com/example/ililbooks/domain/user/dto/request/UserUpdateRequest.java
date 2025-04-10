package com.example.ililbooks.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import static com.example.ililbooks.global.dto.ValidationMessage.*;

public record UserUpdateRequest(@NotBlank(message = NOT_BLANK_NICKNAME) String nickname,
                                @NotBlank(message = NOT_BLANK_ZIPCODE) @Pattern(regexp = PATTERN_ZIPCODE_REGEXP, message = PATTERN_ZIPCODE) String zipCode,
                                @NotBlank(message = NOT_BLANK_ROAD_ADDRESS) String roadAddress,
                                @NotBlank(message = NOT_BLANK_DETAILED_ADDRESS) String detailedAddress,
                                @NotBlank(message = NOT_BLANK_CONTACT_NUMBER) @Size(min = 10, max = 11, message = VALIDATE_CONTACT_NUMBER_SIZE) String contactNumber) {

    @Builder
    public UserUpdateRequest(String nickname, String zipCode, String roadAddress, String detailedAddress, String contactNumber) {
        this.nickname = nickname;
        this.zipCode = zipCode;
        this.roadAddress = roadAddress;
        this.detailedAddress = detailedAddress;
        this.contactNumber = contactNumber;
    }
}
