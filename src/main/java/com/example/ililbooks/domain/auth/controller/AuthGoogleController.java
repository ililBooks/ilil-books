package com.example.ililbooks.domain.auth.controller;

import com.example.ililbooks.client.google.dto.GoogleApiResponse;
import com.example.ililbooks.domain.auth.dto.request.AuthGoogleAccessTokenRequest;
import com.example.ililbooks.domain.auth.dto.response.AuthAccessTokenResponse;
import com.example.ililbooks.domain.auth.dto.response.AuthTokensResponse;
import com.example.ililbooks.domain.auth.service.AuthGoogleService;
import com.example.ililbooks.global.dto.response.Response;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.example.ililbooks.config.util.JwtUtil.REFRESH_TOKEN_TIME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/google")
public class AuthGoogleController {

    private final AuthGoogleService authGoogleService;

    /* 로그인 인증 요청 */
    @GetMapping("/request")
    public Response<URI> createAuthorizationUrl() {
        return Response.of(authGoogleService.createAuthorizationUrl());
    }

    /* 접근 토큰 발급 */
    @PostMapping("/token")
    public Response<GoogleApiResponse> requestToken(
            @RequestParam String code
    ) {
        return Response.of(authGoogleService.requestToken(code));
    }

    /* 회원가입 */
    @PostMapping("/sign-up")
    public Response<AuthAccessTokenResponse> signUp(
            @RequestBody AuthGoogleAccessTokenRequest authGoogleAccessTokenRequest,
            HttpServletResponse httpServletResponse
    ) {
        AuthTokensResponse tokensResponseDto = authGoogleService.signUp(authGoogleAccessTokenRequest);
        setRefreshTokenCookie(httpServletResponse, tokensResponseDto.refreshToken());

        return Response.of(AuthAccessTokenResponse.of(tokensResponseDto.accessToken()));
    }

    /* 로그인 */
    @PostMapping("/sign-in")
    public Response<AuthAccessTokenResponse> signIn(
            @RequestBody AuthGoogleAccessTokenRequest authGoogleAccessTokenRequest,
            HttpServletResponse httpServletResponse
    ) {
        AuthTokensResponse tokensResponseDto = authGoogleService.signIn(authGoogleAccessTokenRequest);
        setRefreshTokenCookie(httpServletResponse, tokensResponseDto.refreshToken());

        return Response.of(AuthAccessTokenResponse.of(tokensResponseDto.accessToken()));
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
