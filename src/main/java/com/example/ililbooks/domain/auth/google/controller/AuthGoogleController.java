package com.example.ililbooks.domain.auth.google.controller;

import com.example.ililbooks.client.google.dto.GoogleApiResponse;
import com.example.ililbooks.domain.auth.google.dto.request.AuthGoogleAccessTokenRequest;
import com.example.ililbooks.domain.auth.dto.response.AuthAccessTokenResponse;
import com.example.ililbooks.domain.auth.dto.response.AuthTokensResponse;
import com.example.ililbooks.domain.auth.google.service.AuthGoogleService;
import com.example.ililbooks.global.dto.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.example.ililbooks.config.util.JwtUtil.REFRESH_TOKEN_TIME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/google")
@Tag(name = "Google", description = "Google 소셜 로그인과 관련된 API")
public class AuthGoogleController {

    private final AuthGoogleService authGoogleService;

    /* 로그인 인증 요청 */
    @Operation(summary = "구글 로그인 인증 요청", description = "구글 로그인 인증 요청을 위한 API입니다.")
    @GetMapping
    public Response<URI> createAuthorizationUrl() {
        return Response.of(authGoogleService.createAuthorizationUrl());
    }

    /* 접근 토큰 발급 */
    @Operation(summary = "구글 접근 토큰 발급", description = "redirect_uri를 통해 얻은 code로 접근 토근 발급하는 API 입니다.")
    @PostMapping("/token")
    public Response<GoogleApiResponse> requestToken(
            @RequestParam String code
    ) {
        return Response.of(authGoogleService.requestToken(code));
    }

    /* 회원가입 */
    @Operation(summary = "구글을 통한 회원가입", description = "접근 토근을 통해 프로필을 조회한 후 해당 값으로 회원가입을 하는 API입니다.")
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
    @Operation(summary = "구글을 통한 로그인", description = "DB에 저장된 유저를 통해 로그인 진행")
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
    //TODO 이거 공통 util로 뺴세요 모든 소셜에 이게 있네요
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setMaxAge(REFRESH_TOKEN_TIME);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        response.addCookie(cookie);
    }
}
