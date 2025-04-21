package com.example.ililbooks.domain.auth.service;

import com.example.ililbooks.domain.auth.dto.request.AuthSignInRequest;
import com.example.ililbooks.domain.auth.dto.request.AuthSignUpRequest;
import com.example.ililbooks.domain.auth.dto.response.AuthTokensResponse;
import com.example.ililbooks.domain.auth.entity.RefreshToken;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.LoginType;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.ililbooks.domain.user.enums.LoginType.EMAIL;
import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    /* 회원가입 */
    @Transactional
    public AuthTokensResponse signUp(AuthSignUpRequest request) {

        if (!request.password().equals(request.passwordCheck())) {
            throw new BadRequestException(PASSWORD_CONFIRMATION_MISMATCH.getMessage());
        }

        Users users = userService.saveUser(request);

        return getTokenResponse(users);
    }

    /* 로그인 */
    @Transactional(readOnly = true)
    public AuthTokensResponse signIn(AuthSignInRequest request) {
        Users users = userService.findByEmailAndLoginTypeOrElseThrow(request.email(), EMAIL);

        if (users.isDeleted()) {
            throw new UnauthorizedException(DEACTIVATED_USER_EMAIL.getMessage());
        }

        if (!passwordEncoder.matches(request.password(), users.getPassword())) {
            throw new UnauthorizedException(INVALID_PASSWORD.getMessage());
        }

        return getTokenResponse(users);
    }

    /* Access Token, Refresh Token 재발급 */
    @Transactional(readOnly = true)
    public AuthTokensResponse reissueToken(String refreshToken) {
        RefreshToken findRefreshToken = tokenService.findRefreshToken(refreshToken);
        Users users = userService.findByIdOrElseThrow(findRefreshToken.getUserId());

        String reissuedAccessToken = tokenService.createAccessToken(users);
        String reissuedRefreshToken = tokenService.updateRefreshToken(findRefreshToken);

        return AuthTokensResponse.of(reissuedAccessToken, reissuedRefreshToken);
    }

    /* Access Token, Refresh Token 생성 및 저장 */
    public AuthTokensResponse getTokenResponse(Users users) {

        String accessToken = tokenService.createAccessToken(users);
        String refreshToken = tokenService.createRefreshToken(users);

        return AuthTokensResponse.of(accessToken, refreshToken);
    }
}
