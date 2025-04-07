package com.example.ililbooks.domain.user.dto.response;

import com.example.ililbooks.domain.user.entity.Users;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserResponse {

    private final Long id;
    private final String email;
    private final String nickname;
    private final String zipCode;
    private final String roadAddress;
    private final String detailedAddress;
    private final String contactNumber;
    private final String loginType;

    @Builder
    private UserResponse(Long id, String email, String nickname, String zipCode, String roadAddress, String detailedAddress, String contactNumber, String loginType) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.zipCode = zipCode;
        this.roadAddress = roadAddress;
        this.detailedAddress = detailedAddress;
        this.contactNumber = contactNumber;
        this.loginType = loginType;
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
