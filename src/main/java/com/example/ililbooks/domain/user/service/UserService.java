package com.example.ililbooks.domain.user.service;

import com.example.ililbooks.config.util.JwtUtil;
import com.example.ililbooks.domain.auth.dto.request.AuthSignUpRequest;
import com.example.ililbooks.domain.auth.dto.response.AuthAccessTokenResponse;
import com.example.ililbooks.domain.user.dto.request.UserDeleteRequest;
import com.example.ililbooks.domain.user.dto.request.UserUpdatePasswordRequest;
import com.example.ililbooks.domain.user.dto.request.UserUpdateRequest;
import com.example.ililbooks.domain.user.dto.response.UserResponse;
import com.example.ililbooks.domain.user.entity.Users;
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
    public Users saveUser(AuthSignUpRequest authSignupRequest) {

        if (userRepository.existsByEmail(authSignupRequest.getEmail())) {
            throw new BadRequestException(DUPLICATE_EMAIL.getMessage());
        }

        String encodedPassword = passwordEncoder.encode(authSignupRequest.getPassword());

        Users users = Users.of(authSignupRequest, encodedPassword);

        return userRepository.save(users);
    }

    /* 회원 조회 */
    @Transactional(readOnly = true)
    public UserResponse getUser(AuthUser authUser) {
        Users findUsers = findByIdOrElseThrow(authUser.getUserId());
        return UserResponse.of(findUsers);
    }

    /* 회원 수정 */
    @Transactional
    public AuthAccessTokenResponse updateUser(AuthUser authUser, UserUpdateRequest userUpdateRequest) {
        Users findUsers = findByIdOrElseThrow(authUser.getUserId());
        findUsers.updateUser(userUpdateRequest);

        String accessToken = jwtUtil.createAccessToken(findUsers.getId(), findUsers.getEmail(), findUsers.getNickname(), findUsers.getUserRole());

        return AuthAccessTokenResponse.of(accessToken);
    }

    /* 회원 비밀번호 수정 */
    @Transactional
    public void updatePasswordUser(AuthUser authUser, UserUpdatePasswordRequest userUpdatePasswordRequest) {

        if (!userUpdatePasswordRequest.getNewPassword().equals(userUpdatePasswordRequest.getNewPasswordCheck())) {
            throw new BadRequestException(PASSWORD_CONFIRMATION_MISMATCH.getMessage());
        }
        //TODO findUsers 네이밍 좀 이상 existUsers ..
        Users findUsers = findByIdOrElseThrow(authUser.getUserId());

        if (!passwordEncoder.matches(userUpdatePasswordRequest.getOldPassword(), findUsers.getPassword())) {
            throw new BadRequestException(INVALID_PASSWORD.getMessage());
        }

        findUsers.updatePassword(passwordEncoder.encode(userUpdatePasswordRequest.getNewPassword()));
    }

    /* 회원 삭제 */
    @Transactional
    public void deleteUser(AuthUser authUser, UserDeleteRequest userDeleteRequest) {
        Users findUsers = findByIdOrElseThrow(authUser.getUserId());

        if (!passwordEncoder.matches(userDeleteRequest.getPassword(), findUsers.getPassword())) {
            throw new BadRequestException(INVALID_PASSWORD.getMessage());
        }
        //TODO 도메인 로직으로 뺼 필요없음
        findUsers.deleteUser();
    }

    public Users findByEmailOrElseThrow(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UnauthorizedException(USER_EMAIL_NOT_FOUND.getMessage())
        );
    }

    public Users findByIdOrElseThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(USER_ID_NOT_FOUND.getMessage())
        );
    }
}
