package com.example.ililbooks.domain.auth.service;

import com.example.ililbooks.client.NaverClient;
import com.example.ililbooks.client.dto.NaverProfileResponse;
import com.example.ililbooks.client.dto.NaverResponse;
import com.example.ililbooks.domain.auth.dto.request.AuthNaverRefreshRequest;
import com.example.ililbooks.domain.auth.dto.request.AuthNaverReqeust;
import com.example.ililbooks.domain.auth.dto.response.AuthTokensResponse;
import com.example.ililbooks.domain.auth.entity.RefreshToken;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.LoginType;
import com.example.ililbooks.domain.user.repository.UserRepository;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.global.exception.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;

import static com.example.ililbooks.domain.user.enums.LoginType.NAVER;
import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class NaverService {

    private final NaverClient naverClient;
    private final UserService userService;
    private final AuthService authService;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Transactional
    public URI getNaverLoginRedirectUrl() {
        return naverClient.getNaverLoginRedirectUrl();
    }

    @Transactional
    public NaverResponse requestToken(String code, String state) {
        return naverClient.findToken(code, state);
    }

    @Transactional
    public NaverResponse refreshNaverToken(AuthNaverRefreshRequest authNaverRefreshRequest) {
        return naverClient.findRefreshToken(authNaverRefreshRequest.refreshToken());
    }

    @Transactional
    public AuthTokensResponse naverSignUp(AuthNaverReqeust authNaverReqeust) {
        NaverProfileResponse profile = naverClient.findProfile(authNaverReqeust.accessToken());

        //등록된 유저가 있는 경우
        if (userService.existsByEmail(profile.email())) {
            throw new BadRequestException(DUPLICATE_EMAIL.getMessage());
        }

        Users users = Users.of(profile.email(), profile.nickname(), profile.mobile(), NAVER);

        userRepository.save(users);
        return authService.getTokenResponse(users);
    }

    @Transactional
    public AuthTokensResponse naverSignIn(AuthNaverReqeust authNaverReqeust) {
        NaverProfileResponse profile = naverClient.findProfile(authNaverReqeust.accessToken());

        Users users = userService.findByEmailOrElseThrow(profile.email());

        if (users.isDeleted()) {
            throw new UnauthorizedException(DEACTIVATED_USER_EMAIL.getMessage());
        }

        if (!NAVER.equals(users.getLoginType())) {
            throw new UnauthorizedException(NOT_NAVER_USER.getMessage());
        }

        return authService.getTokenResponse(users);
    }
}
