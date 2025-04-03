package com.example.ililbooks.domain.auth.service;

import com.example.ililbooks.domain.auth.dto.request.AuthSigninRequestDto;
import com.example.ililbooks.domain.auth.dto.request.AuthSignupRequestDto;
import com.example.ililbooks.domain.auth.dto.response.AuthTokensResponseDto;
import com.example.ililbooks.domain.user.entity.User;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    /* 회원가입 */
    @Transactional
    public AuthTokensResponseDto signup(AuthSignupRequestDto request) {

        if (!request.getPassword().equals(request.getPasswordCheck())) {
            throw new BadRequestException(PASSWORD_CONFIRMATION_MISMATCH.getMessage());
        }

        User user = userService.saveUser(request.getEmail(), request.getNickname(), request.getPassword(), request.getUserRole());

        return getTokenResponse(user);
    }

    /* 로그인 */
    @Transactional
    public AuthTokensResponseDto signin(AuthSigninRequestDto request) {
        User user = userService.getUserByEmail(request.getEmail());

        if (user.getDeletedAt() != null) {
            throw new UnauthorizedException(DEACTIVATED_USER_EMAIL.getMessage());
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException(INVALID_PASSWORD.getMessage());
        }

        return getTokenResponse(user);
    }

    /* Access Token, Refresh Token 재발급 */
    @Transactional
    public AuthTokensResponseDto reissueAccessToken(String refreshToken) {
        User user = tokenService.reissueToken(refreshToken);

        return getTokenResponse(user);
    }

    /* Access Token, Refresh Token 생성 및 저장 */
    private AuthTokensResponseDto getTokenResponse(User user) {

        String accessToken = tokenService.createAccessToken(user);
        String refreshToken = tokenService.createRefreshToken(user);

        return AuthTokensResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
