package com.example.ililbooks.domain.auth.controller;

import com.example.ililbooks.config.annotation.RefreshToken;
import com.example.ililbooks.domain.auth.dto.request.AuthSignInRequest;
import com.example.ililbooks.domain.auth.dto.request.AuthSignUpRequest;
import com.example.ililbooks.domain.auth.dto.response.AuthAccessTokenResponse;
import com.example.ililbooks.domain.auth.dto.response.AuthTokensResponse;
import com.example.ililbooks.domain.auth.service.AuthService;
import com.example.ililbooks.global.dto.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import static com.example.ililbooks.config.util.CookieUtil.addRefreshTokenCookie;
import static com.example.ililbooks.domain.user.enums.UserRole.Authority.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/email")
@Tag(name = "Auth", description = "이메일 회원가입 및 로그인과 관련된 API")
public class AuthController {

    private final AuthService authService;

    /* 회원가입 */
    @Operation(summary = "이메일 회원가입", description = "이메일을 통한 회원가입에 대한 API입니다.")
    @PostMapping("/sign-up")
    public Response<AuthAccessTokenResponse> signUp(
            @Valid @RequestBody AuthSignUpRequest request,
            HttpServletResponse httpServletResponse
    ) {
        AuthTokensResponse tokensResponseDto = authService.signUp(request);

        addRefreshTokenCookie(httpServletResponse, tokensResponseDto.refreshToken());

        return Response.of(AuthAccessTokenResponse.of(tokensResponseDto.accessToken()));
    }

    /* 로그인 */
    @Operation(summary = "이메일 로그인", description = "이메일을 통한 로그인에 대한 API입니다.")
    @PostMapping("/sign-in")
    public Response<AuthAccessTokenResponse> signIn(
            @Valid @RequestBody AuthSignInRequest request,
            HttpServletResponse httpServletResponse
    ) {
        AuthTokensResponse tokensResponseDto = authService.signIn(request);

        addRefreshTokenCookie(httpServletResponse, tokensResponseDto.refreshToken());

        return Response.of(AuthAccessTokenResponse.of(tokensResponseDto.accessToken()));
    }

    /* 토큰 재발급 (로그인 기간 연장) */
    @Operation(summary = "토큰 재발급", description = "Access 및 Refresh Token 재발급에 대한 API입니다.")
    @Secured({USER, PUBLISHER, ADMIN})
    @GetMapping("/refresh")
    public Response<AuthAccessTokenResponse> refresh(
            @RefreshToken String refreshToken,
            HttpServletResponse httpServletResponse
    ) {
        AuthTokensResponse tokensResponseDto = authService.reissueToken(refreshToken);

        addRefreshTokenCookie(httpServletResponse, tokensResponseDto.refreshToken());

        return Response.of(AuthAccessTokenResponse.of(tokensResponseDto.accessToken()));
    }
}
