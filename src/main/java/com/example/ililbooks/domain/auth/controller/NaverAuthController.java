package com.example.ililbooks.domain.auth.controller;

import com.example.ililbooks.client.dto.NaverProfileResponse;
import com.example.ililbooks.client.dto.NaverResponse;
import com.example.ililbooks.domain.auth.dto.request.AuthNaverRefreshRequest;
import com.example.ililbooks.domain.auth.dto.request.AuthNaverReqeust;
import com.example.ililbooks.domain.auth.dto.response.AuthAccessTokenResponse;
import com.example.ililbooks.domain.auth.dto.response.AuthTokensResponse;
import com.example.ililbooks.domain.auth.service.NaverService;
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
@RequestMapping("/api/v1/auth/naver")
@Tag(name = "Naver", description = "Naver 로그인 및 회원가입, 토큰 재발급과 관련된 API")
public class NaverAuthController {
    private final NaverService naverService;
    private final AuthController authController;

    /**
     * 네이버 로그인 인증 요청을 위한 API 입니다.
     */
    @Operation(summary = "네이버 로그인 인증 요청", description = "네이버 로그인 API에서 로그인 인증 요청을 위한 API입니다.")
    @PostMapping
    public Response<URI> getNaverLoginRedirectUrl () {
        return Response.of(naverService.getNaverLoginRedirectUrl());
    }

    /**
     * 로그인 인증 성공 후 접근 토큰 발급 요청을 하는 API 입니다.
     */
    @Operation(summary = "네이버 접근 토큰 발급", description = "redirect_uri를 통해 얻은 code, state로 접근 토근 발급")
    @PostMapping("/token")
    public Response<NaverResponse> requestToken(
            @RequestParam String code,
            @RequestParam String state
    ) {
        return Response.of(naverService.requestToken(code, state));
    }

    /**
     * 접근 토큰 발급 후 해당 토큰으로 프로필을 조회한 후 회원가입을 하는 API입니다.
     */
    @Operation(summary = "네이버를 통한 회원가입", description = "접근 토근을 통해 프로필을 조회한 후 해당 값으로 회원가입을 진행한다.")
    @PostMapping("/sign-up")
    public Response<AuthAccessTokenResponse> naverSignUp(
            @RequestBody AuthNaverReqeust authNaverReqeust,
            HttpServletResponse httpServletResponse
    ) {
        AuthTokensResponse tokensResponseDto = naverService.naverSignUp(authNaverReqeust);
        authController.setRefreshTokenCookie(httpServletResponse, tokensResponseDto.refreshToken());

        return Response.of(AuthAccessTokenResponse.of(tokensResponseDto.accessToken()));
    }

    /**
     * 접근 토큰 재발급
     */
    @Operation(summary = "접근 토큰 재발급", description = "접근 토큰을 갱신합니다.")
    @PostMapping("/refresh")
    public Response<NaverResponse> refreshNaverToken(
            @RequestBody AuthNaverRefreshRequest authNaverRefreshRequest
    ) {
        return Response.of(naverService.refreshNaverToken(authNaverRefreshRequest));

    }

    /**
     *
     */
    @Operation(summary = "네이버를 통한 로그인", description = "DB에 저장된 유저를 통해 로그인 진행")
    @PostMapping("/sign-in")
    public Response<AuthAccessTokenResponse> naverSignIn(
            @RequestBody AuthNaverReqeust authNaverReqeust,
            HttpServletResponse httpServletResponse
    ) {
        AuthTokensResponse tokensResponseDto = naverService.naverSignIn(authNaverReqeust);
        authController.setRefreshTokenCookie(httpServletResponse, tokensResponseDto.refreshToken());

        return Response.of(AuthAccessTokenResponse.of(tokensResponseDto.accessToken()));
    }
}
