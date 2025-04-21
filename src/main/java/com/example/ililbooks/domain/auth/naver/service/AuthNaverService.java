package com.example.ililbooks.domain.auth.naver.service;

import com.example.ililbooks.client.naver.NaverClient;
import com.example.ililbooks.client.naver.dto.NaverApiProfileResponse;
import com.example.ililbooks.client.naver.dto.NaverApiResponse;
import com.example.ililbooks.domain.auth.naver.dto.request.AuthNaverAccessTokenRequest;
import com.example.ililbooks.domain.auth.dto.response.AuthTokensResponse;
import com.example.ililbooks.domain.auth.service.AuthService;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.LoginType;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.domain.user.service.UserSocialService;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

import static com.example.ililbooks.domain.user.enums.LoginType.NAVER;
import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class AuthNaverService {

    private final NaverClient naverClient;
    private final UserService userService;
    private final AuthService authService;
    private final UserSocialService userSocialService;

    public URI getNaverLoginRedirectUrl() {
        return naverClient.getRedirectUrl();
    }

    public NaverApiResponse requestNaverToken(String code, String state) {
        return naverClient.issueToken(code, state);
    }

    @Transactional
    public AuthTokensResponse signUpWithNaver(AuthNaverAccessTokenRequest authNaverAccessTokenRequest) {
        NaverApiProfileResponse profile = getProfile(authNaverAccessTokenRequest);

        if (userService.existsByEmailAndLoginType(profile.email(), NAVER)) {
            throw new BadRequestException(DUPLICATE_EMAIL.getMessage());
        }

        Users users = Users.of(profile.email(), profile.nickname(), profile.mobile(), NAVER);

        userSocialService.saveUser(users);
        return authService.getTokenResponse(users);
    }

    @Transactional(readOnly = true)
    public AuthTokensResponse signInWithNaver(AuthNaverAccessTokenRequest authNaverAccessTokenRequest) {
        NaverApiProfileResponse profile = getProfile(authNaverAccessTokenRequest);
        Users users = userService.findByEmailOrElseThrow(profile.email());

        if (users.isDeleted()) {
            throw new UnauthorizedException(DEACTIVATED_USER_EMAIL.getMessage());
        }

        if (!NAVER.equals(users.getLoginType())) {
            throw new UnauthorizedException(NOT_NAVER_USER.getMessage());
        }

        return authService.getTokenResponse(users);
    }

    private NaverApiProfileResponse getProfile(AuthNaverAccessTokenRequest authNaverAccessTokenRequest) {
        return naverClient.findProfile(authNaverAccessTokenRequest.accessToken());
    }
}
