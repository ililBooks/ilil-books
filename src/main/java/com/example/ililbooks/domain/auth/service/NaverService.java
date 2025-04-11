package com.example.ililbooks.domain.auth.service;

import com.example.ililbooks.client.NaverClient;
import com.example.ililbooks.client.dto.NaverApiProfileResponse;
import com.example.ililbooks.client.dto.NaverApiResponse;
import com.example.ililbooks.domain.auth.dto.request.AuthNaverRefreshTokenRequest;
import com.example.ililbooks.domain.auth.dto.request.AuthNaverAccessTokenRequest;
import com.example.ililbooks.domain.auth.dto.response.AuthTokensResponse;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.repository.UserRepository;
import com.example.ililbooks.domain.user.service.UserService;
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
public class NaverService {

    private final NaverClient naverClient;
    private final UserService userService;
    private final AuthService authService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public URI getNaverLoginRedirectUrl() {
        return naverClient.getNaverLoginRedirectUrl();
    }

    @Transactional
    public NaverApiResponse requestToken(String code, String state) {
        return naverClient.issueToken(code, state);
    }

    @Transactional
    public NaverApiResponse refreshNaverToken(AuthNaverRefreshTokenRequest authNaverRefreshTokenRequest) {
        return naverClient.refreshToken(authNaverRefreshTokenRequest.refreshToken());
    }

    @Transactional
    public AuthTokensResponse naverSignUp(AuthNaverAccessTokenRequest authNaverAccessTokenRequest) {
        NaverApiProfileResponse profile = getProfile(authNaverAccessTokenRequest);

        if (userService.existsByEmail(profile.email())) {
            throw new BadRequestException(DUPLICATE_EMAIL.getMessage());
        }

        Users users = Users.of(profile.email(), profile.nickname(), profile.mobile(), NAVER);

        userRepository.save(users);
        return authService.getTokenResponse(users);
    }

    @Transactional
    public AuthTokensResponse naverSignIn(AuthNaverAccessTokenRequest authNaverAccessTokenRequest) {
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
