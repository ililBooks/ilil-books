package com.example.ililbooks.domain.user.service;

import com.example.ililbooks.config.util.JwtUtil;
import com.example.ililbooks.domain.auth.dto.request.AuthSignUpRequest;
import com.example.ililbooks.domain.auth.dto.response.AuthAccessTokenResponse;
import com.example.ililbooks.domain.user.dto.request.UserDeleteRequest;
import com.example.ililbooks.domain.user.dto.request.UserUpdateAlertRequest;
import com.example.ililbooks.domain.user.dto.request.UserUpdatePasswordRequest;
import com.example.ililbooks.domain.user.dto.request.UserUpdateRequest;
import com.example.ililbooks.domain.user.dto.response.UserResponse;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.LoginType;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.domain.user.repository.UserRepository;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /* 회원 저장 */
    @Transactional
    public Users saveUser(AuthSignUpRequest request) {

        if (existsByEmailAndLoginType(request.email(), LoginType.EMAIL)) {
            throw new BadRequestException(DUPLICATE_EMAIL.getMessage());
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        Users users = Users.of(request.email(), request.nickname(), encodedPassword, request.userRole(), LoginType.EMAIL);

        return userRepository.save(users);
    }

    /* 회원 조회 */
    @Transactional(readOnly = true)
    public UserResponse findUser(AuthUser authUser) {
        Users users = findByIdOrElseThrow(authUser.getUserId());
        return UserResponse.of(users);
    }

    /* 회원 수정 */
    @Transactional
    public AuthAccessTokenResponse updateUser(AuthUser authUser, UserUpdateRequest userUpdateRequest) {
        Users users = findByIdOrElseThrow(authUser.getUserId());
        users.updateUser(userUpdateRequest.nickname(),
                userUpdateRequest.zipCode(),
                userUpdateRequest.roadAddress(),
                userUpdateRequest.detailedAddress(),
                userUpdateRequest.contactNumber());

        String accessToken = jwtUtil.createAccessToken(users.getId(), users.getEmail(), users.getNickname(), users.getUserRole());

        return AuthAccessTokenResponse.of(accessToken);
    }

    /* 회원 비밀번호 수정 */
    @Transactional
    public void updatePasswordUser(AuthUser authUser, UserUpdatePasswordRequest userUpdatePasswordRequest) {

        if (!userUpdatePasswordRequest.newPassword().equals(userUpdatePasswordRequest.newPasswordCheck())) {
            throw new BadRequestException(PASSWORD_CONFIRMATION_MISMATCH.getMessage());
        }

        Users users = findByIdOrElseThrow(authUser.getUserId());

        if (!passwordEncoder.matches(userUpdatePasswordRequest.oldPassword(), users.getPassword())) {
            throw new BadRequestException(INVALID_PASSWORD.getMessage());
        }

        users.updatePassword(passwordEncoder.encode(userUpdatePasswordRequest.newPassword()));
    }

    /* 회원 삭제 */
    @Transactional
    public void deleteUser(AuthUser authUser, UserDeleteRequest userDeleteRequest) {
        Users users = findByIdOrElseThrow(authUser.getUserId());

        if (users.getLoginType() == LoginType.EMAIL
                && !passwordEncoder.matches(userDeleteRequest.password(), users.getPassword())) {
            throw new BadRequestException(INVALID_PASSWORD.getMessage());
        }

        users.deleteUser();
    }

    /* 알림 수신 동의 및 거부*/
    @Transactional
    public void updateAlert(AuthUser authUser, boolean receive) {
        Users users = findByEmailOrElseThrow(authUser.getEmail());
        users.updateAlert(receive);
    }

    public Users findByEmailOrElseThrow(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UnauthorizedException(USER_EMAIL_NOT_FOUND.getMessage())
        );
    }

    public Users findByEmailAndLoginTypeOrElseThrow(String email, LoginType loginType) {
        return userRepository.findByEmailAndLoginType(email, loginType).orElseThrow(
                () -> new UnauthorizedException(USER_EMAIL_NOT_FOUND.getMessage())
        );
    }

    public Users findByIdOrElseThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(USER_ID_NOT_FOUND.getMessage())
        );
    }

    public boolean existsByEmailAndLoginType(String email, LoginType loginType) {
        return userRepository.existsByEmailAndLoginType(email, loginType);
    }

    /*
    * 로컬 db 에서 email 로 사용자 조회
    * 있는 경우 Users 객체 반환
    * 없을 경우 email, nickname, loginType, userRole 값의 유저 생성 후 저장
    *  */
    public Users findByEmailOrGet(String email, String nickname, LoginType loginType, UserRole userRole) {
        return userRepository.findByEmailAndLoginType(email, loginType)
                .orElseGet(() -> userRepository.save(
                        Users.builder()
                                .email(email)
                                .nickname(nickname)
                                .loginType(loginType)
                                .userRole(userRole)
                                .build()
                ));
    }
}
