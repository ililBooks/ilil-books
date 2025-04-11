package com.example.ililbooks.domain.auth.service;

import com.example.ililbooks.client.google.GoogleClient;
import com.example.ililbooks.client.google.dto.GoogleApiProfileResponse;
import com.example.ililbooks.client.google.dto.GoogleApiResponse;
import com.example.ililbooks.client.naver.dto.NaverApiProfileResponse;
import com.example.ililbooks.domain.auth.dto.request.AuthGoogleAccessTokenRequest;
import com.example.ililbooks.domain.auth.dto.request.AuthNaverAccessTokenRequest;
import com.example.ililbooks.domain.auth.dto.response.AuthTokensResponse;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.domain.user.service.UserSocialService;
import com.example.ililbooks.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

import static com.example.ililbooks.domain.user.enums.LoginType.GOOGLE;
import static com.example.ililbooks.global.exception.ErrorMessage.DUPLICATE_EMAIL;

@Service
@RequiredArgsConstructor
public class AuthGoogleService {

    private final GoogleClient googleClient;
    private final UserService userService;
    private final AuthService authService;
    private final UserSocialService userSocialService;

    /* 로그인 인증 요청 */
    public URI createAuthorizationUrl() {
        return googleClient.createAuthorizationUrl();
    }

    /* 토큰 요청 */
    public GoogleApiResponse requestToken(String code) {
        return googleClient.issueToken(code);
    }

    /*  */
    @Transactional
    public AuthTokensResponse signUp(AuthGoogleAccessTokenRequest authGoogleAccessTokenRequest) {
        GoogleApiProfileResponse profile = getProfile(authGoogleAccessTokenRequest);

        if (userService.existsByEmail(profile.email())) {
            throw new BadRequestException(DUPLICATE_EMAIL.getMessage());
        }

        Users users = Users.of(profile.email(), profile.name(), GOOGLE);

        userSocialService.saveUser(users);
        return authService.getTokenResponse(users);
    }

    private GoogleApiProfileResponse getProfile(AuthGoogleAccessTokenRequest authGoogleAccessTokenRequest) {
        return googleClient.findProfile(authGoogleAccessTokenRequest.accessToken());
    }
}
