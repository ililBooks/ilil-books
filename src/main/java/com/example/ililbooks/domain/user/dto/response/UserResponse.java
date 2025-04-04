package com.example.ililbooks.domain.user.dto.response;

import com.example.ililbooks.domain.user.entity.User;
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

    public static UserResponse of(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .zipCode(user.getZipCode())
                .roadAddress(user.getRoadAddress())
                .detailedAddress(user.getDetailedAddress())
                .contactNumber(user.getContactNumber())
                .loginType(user.getLoginType().name())
                .build();
    }
}
