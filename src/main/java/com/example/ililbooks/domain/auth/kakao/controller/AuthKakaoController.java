package com.example.ililbooks.domain.auth.kakao.controller;

import com.example.ililbooks.domain.auth.kakao.dto.response.AuthKakaoTokenResponse;
import com.example.ililbooks.domain.auth.kakao.service.AuthkakaoService;
import com.example.ililbooks.global.dto.response.Response;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.example.ililbooks.config.util.JwtUtil.REFRESH_TOKEN_TIME;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth/kakao")
public class AuthKakaoController {

    private final AuthkakaoService authkakaoService;

    @GetMapping("/token")
    public Response<AuthKakaoTokenResponse> signInWithKakao(@RequestParam String code,
                                                            HttpServletResponse httpServletResponse) {
        AuthKakaoTokenResponse authKakaoTokenResponse = authkakaoService.signInWithKakao(code);
        setRefreshTokenCookie(httpServletResponse, authKakaoTokenResponse.refreshToken());
        return Response.of(authKakaoTokenResponse);
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
