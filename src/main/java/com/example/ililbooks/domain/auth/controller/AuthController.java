package com.example.ililbooks.domain.auth.controller;

import com.example.ililbooks.config.annotation.RefreshToken;
import com.example.ililbooks.domain.auth.dto.request.AuthSignInRequest;
import com.example.ililbooks.domain.auth.dto.request.AuthSignUpRequest;
import com.example.ililbooks.domain.auth.dto.response.AuthAccessTokenResponse;
import com.example.ililbooks.domain.auth.dto.response.AuthTokensResponse;
import com.example.ililbooks.domain.auth.service.AuthService;
import com.example.ililbooks.global.dto.response.Response;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import static com.example.ililbooks.config.util.JwtUtil.REFRESH_TOKEN_TIME;
import static com.example.ililbooks.domain.user.enums.UserRole.Authority.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    /* 회원가입 */
    @PostMapping("/sign-up")
    public Response<AuthAccessTokenResponse> signUp(
            @Valid @RequestBody AuthSignUpRequest request,
            HttpServletResponse httpServletResponse
    ) {
        AuthTokensResponse tokensResponseDto = authService.signUp(request);

        setRefreshTokenCookie(httpServletResponse, tokensResponseDto.getRefreshToken());

        return Response.of(AuthAccessTokenResponse.of(tokensResponseDto.getAccessToken()));
    }

    /* 로그인 */
    @PostMapping("/sign-in")
    public Response<AuthAccessTokenResponse> signIn(
            @Valid @RequestBody AuthSignInRequest request,
            HttpServletResponse httpServletResponse
    ) {
        AuthTokensResponse tokensResponseDto = authService.signIn(request);

        setRefreshTokenCookie(httpServletResponse, tokensResponseDto.getRefreshToken());

        return Response.of(AuthAccessTokenResponse.of(tokensResponseDto.getAccessToken()));
    }

    /* 토큰 재발급 (로그인 기간 연장) */
    @Secured({USER, PUBLISHER, ADMIN})
    @GetMapping("/refresh")
    public Response<AuthAccessTokenResponse> refresh(
            @RefreshToken String refreshToken,
            HttpServletResponse httpServletResponse
    ) {
        AuthTokensResponse tokensResponseDto = authService.reissueToken(refreshToken);

        setRefreshTokenCookie(httpServletResponse, tokensResponseDto.getRefreshToken());

        return Response.of(AuthAccessTokenResponse.of(tokensResponseDto.getAccessToken()));
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
