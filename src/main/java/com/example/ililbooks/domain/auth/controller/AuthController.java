package com.example.ililbooks.domain.auth.controller;

import com.example.ililbooks.config.annotation.RefreshToken;
import com.example.ililbooks.domain.auth.dto.request.AuthSigninRequestDto;
import com.example.ililbooks.domain.auth.dto.request.AuthSignupRequestDto;
import com.example.ililbooks.domain.auth.dto.response.AuthAccessTokenResponseDto;
import com.example.ililbooks.domain.auth.dto.response.AuthTokensResponseDto;
import com.example.ililbooks.domain.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private static final int REFRESH_TOKEN_TIME = 7 * 24 * 60 * 60; // 7주일

    /* 회원가입 */
    @PostMapping("/v1/auth/signup")
    public AuthAccessTokenResponseDto signup(
            @Valid @RequestBody AuthSignupRequestDto request,
            HttpServletResponse httpServletResponse
    ) {
        AuthTokensResponseDto tokensResponseDto = authService.signup(request);

        setRefreshTokenCookie(httpServletResponse, tokensResponseDto.getRefreshToken());

        return new AuthAccessTokenResponseDto(tokensResponseDto.getAccessToken());
    }

    /* 로그인 */
    @PostMapping("/v1/auth/signin")
    public AuthAccessTokenResponseDto signin(
            @Valid @RequestBody AuthSigninRequestDto request,
            HttpServletResponse httpServletResponse
    ) {
        AuthTokensResponseDto tokensResponseDto = authService.signin(request);

        setRefreshTokenCookie(httpServletResponse, tokensResponseDto.getRefreshToken());

        return new AuthAccessTokenResponseDto(tokensResponseDto.getAccessToken());
    }

    /* 토큰 재발급 (로그인 기간 연장) */
    @Secured({USER, PUBLISHER, ADMIN})
    @GetMapping("/v1/auth/refresh")
    public AuthAccessTokenResponseDto refresh(
            @RefreshToken String refreshToken,
            HttpServletResponse httpServletResponse
    ) {
        AuthTokensResponseDto tokensResponseDto = authService.reissueAccessToken(refreshToken);

        setRefreshTokenCookie(httpServletResponse, tokensResponseDto.getRefreshToken());

        return new AuthAccessTokenResponseDto(tokensResponseDto.getAccessToken());
    }

    /* http only 사용하기 위해 쿠키에 refreshToken 저장 */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setMaxAge(REFRESH_TOKEN_TIME);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        response.addCookie(cookie);
    }


}
