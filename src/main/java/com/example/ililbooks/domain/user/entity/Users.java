package com.example.ililbooks.domain.user.entity;

import com.example.ililbooks.domain.user.enums.LoginType;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.entity.TimeStamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import static com.example.ililbooks.domain.user.enums.UserRole.ROLE_USER;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_email_login_type", columnNames = {"email", "login_type"})
        }
)
public class Users extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String nickname;

    private String password;

    private String zipCode;
    private String roadAddress;
    private String detailedAddress;

    private String contactNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", columnDefinition = "VARCHAR(50)")
    private LoginType loginType;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", columnDefinition = "VARCHAR(50)")
    private UserRole userRole;

    @Temporal(TemporalType.TIMESTAMP)
    private boolean isDeleted;

    @Builder
    private Users(Long id, String email, String nickname, String password, String zipCode, String roadAddress, String detailedAddress, String contactNumber, LoginType loginType, UserRole userRole, boolean isDeleted) {
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
        this.isDeleted = isDeleted;
    }

    public static Users fromAuthUser(AuthUser authUser) {
        return Users.builder()
                .id(authUser.getUserId())
                .email(authUser.getEmail())
                .nickname(authUser.getNickname())
                .userRole(UserRole.of(authUser.getAuthorities().iterator().next().getAuthority()))
                .build();
    }

    public static Users of(String email, String nickname, String encodedPassword, String userRole, LoginType loginType) {
        return Users.builder()
                .email(email)
                .nickname(nickname)
                .password(encodedPassword)
                .loginType(loginType)
                .userRole(UserRole.of(userRole))
                .build();
    }

    public static Users of(String email, String nickname, String contactNumber, LoginType loginType) {
        return Users.builder()
                .email(email)
                .nickname(nickname)
                .contactNumber(contactNumber)
                .loginType(loginType)
                .userRole(ROLE_USER)
                .build();
    }

    public static Users of(String email, String nickname, LoginType loginType) {
        return Users.builder()
                .email(email)
                .nickname(nickname)
                .loginType(loginType)
                .userRole(ROLE_USER)
                .build();
    }

    public void updateUser(String nickname, String zipCode, String roadAddress, String detailedAddress, String contactNumber) {
        this.nickname = nickname;
        this.zipCode = zipCode;
        this.roadAddress = roadAddress;
        this.detailedAddress = detailedAddress;
        this.contactNumber = contactNumber;
    }

    public void deleteUser() {
        this.email = email + "_deleted_" + UUID.randomUUID().toString().substring(0, 6);
        this.isDeleted = true;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
}
