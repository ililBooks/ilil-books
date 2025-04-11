package com.example.ililbooks.domain.auth.controller;

import com.example.ililbooks.client.dto.NaverApiResponse;
import com.example.ililbooks.domain.auth.dto.request.AuthNaverRefreshTokenRequest;
import com.example.ililbooks.domain.auth.dto.request.AuthNaverAccessTokenRequest;
import com.example.ililbooks.domain.auth.dto.response.AuthAccessTokenResponse;
import com.example.ililbooks.domain.auth.dto.response.AuthTokensResponse;
import com.example.ililbooks.domain.auth.service.NaverService;
import com.example.ililbooks.global.dto.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/naver")
@Tag(name = "Naver", description = "Naver 소셜 로그인과 관련된 API")
public class AuthNaverController {
    private final NaverService naverService;
    private final AuthController authController;

    /**
     * 네이버 로그인 인증 요청을 위한 API 입니다.
     */
    @Operation(summary = "네이버 로그인 인증 요청", description = "네이버 로그인 인증 요청을 위한 API입니다.")
    @GetMapping
    public Response<URI> getNaverLoginRedirectUrl () {
        return Response.of(naverService.getNaverLoginRedirectUrl());
    }

    /**
     * 로그인 인증 성공 후 접근 토큰 발급 요청을 하는 API 입니다.
     */
    @Operation(summary = "네이버 접근 토큰 발급", description = "redirect_uri를 통해 얻은 code, state로 접근 토근 발급하는 API 입니다.")
    @PostMapping("/token")
    public Response<NaverApiResponse> requestToken(
            @RequestParam String code,
            @RequestParam String state
    ) {
        return Response.of(naverService.requestToken(code, state));
    }

    /**
     * 접근 토큰 발급 후 해당 토큰으로 프로필을 조회한 후 회원가입을 하는 API입니다.
     */
    @Operation(summary = "네이버를 통한 회원가입", description = "접근 토근을 통해 프로필을 조회한 후 해당 값으로 회원가입을 하는 API입니다.")
    @PostMapping("/sign-up")
    public Response<AuthAccessTokenResponse> naverSignUp(
            @RequestBody AuthNaverAccessTokenRequest authNaverAccessTokenRequest,
            HttpServletResponse httpServletResponse
    ) {
        AuthTokensResponse tokensResponseDto = naverService.naverSignUp(authNaverAccessTokenRequest);
        authController.setRefreshTokenCookie(httpServletResponse, tokensResponseDto.refreshToken());

        return Response.of(AuthAccessTokenResponse.of(tokensResponseDto.accessToken()));
    }

    /**
     * 접근 토큰 재발급
     */
    @Operation(summary = "접근 토큰 재발급", description = "접근 토큰을 재발급하는 API입니다.")
    @PostMapping("/refresh")
    public Response<NaverApiResponse> refreshNaverToken(
            @RequestBody AuthNaverRefreshTokenRequest authNaverRefreshTokenRequest
    ) {
        return Response.of(naverService.refreshNaverToken(authNaverRefreshTokenRequest));

    }

    /**
     *접근 토큰을 통해 프로필 조회 후 해당 정보를 통해 로그인을 하는 API입니다.
     */
    @Operation(summary = "네이버를 통한 로그인", description = "DB에 저장된 유저를 통해 로그인 진행")
    @PostMapping("/sign-in")
    public Response<AuthAccessTokenResponse> naverSignIn(
            @RequestBody AuthNaverAccessTokenRequest authNaverAccessTokenRequest,
            HttpServletResponse httpServletResponse
    ) {
        AuthTokensResponse tokensResponseDto = naverService.naverSignIn(authNaverAccessTokenRequest);
        authController.setRefreshTokenCookie(httpServletResponse, tokensResponseDto.refreshToken());

        return Response.of(AuthAccessTokenResponse.of(tokensResponseDto.accessToken()));
    }
}
