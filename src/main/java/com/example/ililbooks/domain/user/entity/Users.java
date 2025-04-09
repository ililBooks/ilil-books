package com.example.ililbooks.domain.user.entity;

import com.example.ililbooks.domain.auth.dto.request.AuthSignUpRequest;
import com.example.ililbooks.domain.user.dto.request.UserUpdateRequest;
import com.example.ililbooks.domain.user.enums.LoginType;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.entity.TimeStamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class Users extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String nickname;

    private String password;

    private String zipCode;
    private String roadAddress;
    private String detailedAddress;

    private String contactNumber;

    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime deletedAt;

    @Builder
    private Users(Long id, String email, String nickname, String password, String zipCode, String roadAddress, String detailedAddress, String contactNumber, LoginType loginType, UserRole userRole, LocalDateTime deletedAt) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.zipCode = zipCode;
        this.roadAddress = roadAddress;
        this.detailedAddress = detailedAddress;
        this.contactNumber = contactNumber;
        this.loginType = loginType;
        this.userRole = userRole;
        this.deletedAt = deletedAt;
    }

    public static Users fromAuthUser(AuthUser authUser) {
        return Users.builder()
                .id(authUser.getUserId())
                .email(authUser.getEmail())
                .nickname(authUser.getNickname())
                .userRole(UserRole.of(authUser.getAuthorities().iterator().next().getAuthority()))
                .build();
    }

    public static Users of(AuthSignUpRequest authSignupRequest, String encodedPassword) {
        return Users.builder()
                .email(authSignupRequest.getEmail())
                .nickname(authSignupRequest.getNickname())
                .password(encodedPassword)
                .userRole(UserRole.of(authSignupRequest.getUserRole()))
                .build();
    }

    //TODO 파라미터 바꾸기
    public void updateUser(UserUpdateRequest userUpdateRequest) {
        this.nickname = userUpdateRequest.getNickname();
        this.zipCode = userUpdateRequest.getZipCode();
        this.roadAddress = userUpdateRequest.getRoadAddress();
        this.detailedAddress = userUpdateRequest.getDetailedAddress();
        this.contactNumber = userUpdateRequest.getContactNumber();
    }

    public void deleteUser() {
        this.deletedAt = LocalDateTime.now();
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
}
