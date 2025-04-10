package com.example.ililbooks.domain.user.dto.response;

import com.example.ililbooks.domain.user.entity.Users;
import lombok.Builder;

public record UserResponse(Long id, String email, String nickname, String zipCode, String roadAddress,
                           String detailedAddress, String contactNumber, String loginType) {

    @Builder
    public UserResponse {
    }

    public static UserResponse of(Users users) {
        return UserResponse.builder()
                .id(users.getId())
                .email(users.getEmail())
                .nickname(users.getNickname())
                .zipCode(users.getZipCode())
                .roadAddress(users.getRoadAddress())
                .detailedAddress(users.getDetailedAddress())
                .contactNumber(users.getContactNumber())
                .loginType(users.getLoginType().name())
                .build();
    }
}
