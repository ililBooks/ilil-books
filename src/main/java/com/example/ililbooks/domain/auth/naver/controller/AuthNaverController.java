package com.example.ililbooks.domain.auth.naver.controller;

import com.example.ililbooks.client.naver.dto.NaverApiResponse;
import com.example.ililbooks.domain.auth.naver.dto.request.AuthNaverAccessTokenRequest;
import com.example.ililbooks.domain.auth.dto.response.AuthAccessTokenResponse;
import com.example.ililbooks.domain.auth.dto.response.AuthTokensResponse;
import com.example.ililbooks.domain.auth.naver.service.AuthNaverService;
import com.example.ililbooks.global.dto.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.example.ililbooks.config.util.JwtUtil.REFRESH_TOKEN_TIME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/naver")
@Tag(name = "Naver", description = "Naver 소셜 로그인과 관련된 API")
public class AuthNaverController {
    private final AuthNaverService authNaverService;

    /**
     * 네이버 로그인 인증 요청을 위한 API 입니다.
     */
    @Operation(summary = "네이버 로그인 인증 요청", description = "네이버 로그인 인증 요청을 위한 API입니다.")
    @GetMapping
    public Response<URI> getNaverLoginRedirectUrl () {
        return Response.of(authNaverService.getLoginRedirectUrl());
    }

    /**
     * 로그인 인증 성공 후 접근 토큰 발급 요청을 하는 API 입니다.
     */
    @Operation(summary = "네이버 접근 토큰 발급", description = "redirect_uri를 통해 얻은 code, state로 접근 토근 발급하는 API 입니다.")
    @PostMapping("/token")
    public Response<NaverApiResponse> requestNaverToken(
            @RequestParam String code,
            @RequestParam String state,
            HttpSession session
    ) {
        //세션에 저장되어 있던 state값
        String sessionState = (String) session.getAttribute("oauth_state");
        return Response.of(authNaverService.requestToken(code, state, sessionState));
    }

    /**
     * 접근 토큰 발급 후 해당 토큰으로 프로필을 조회한 후 회원가입을 하는 API입니다.
     */
    @Operation(summary = "네이버를 통한 회원가입", description = "접근 토근을 통해 프로필을 조회한 후 해당 값으로 회원가입을 하는 API입니다.")
    @PostMapping("/sign-up")
    public Response<AuthAccessTokenResponse> signUpWithNaver(
            @RequestBody AuthNaverAccessTokenRequest authNaverAccessTokenRequest,
            HttpServletResponse httpServletResponse
    ) {
        AuthTokensResponse tokensResponseDto = authNaverService.signUp(authNaverAccessTokenRequest);
        setRefreshTokenCookie(httpServletResponse, tokensResponseDto.refreshToken());

        return Response.of(AuthAccessTokenResponse.of(tokensResponseDto.accessToken()));
    }

    /**
     *접근 토큰을 통해 프로필 조회 후 해당 정보를 통해 로그인을 하는 API입니다.
     */
    @Operation(summary = "네이버를 통한 로그인", description = "DB에 저장된 유저를 통해 로그인 진행")
    @PostMapping("/sign-in")
    public Response<AuthAccessTokenResponse> signInWithNaver(
            @RequestBody AuthNaverAccessTokenRequest authNaverAccessTokenRequest,
            HttpServletResponse httpServletResponse
    ) {
        AuthTokensResponse tokensResponseDto = authNaverService.signIn(authNaverAccessTokenRequest);
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
