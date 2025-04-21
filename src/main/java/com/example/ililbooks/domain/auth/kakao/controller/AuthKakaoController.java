package com.example.ililbooks.domain.auth.kakao.controller;

import com.example.ililbooks.client.kakao.dto.AuthKakaoTokenResponse;
import com.example.ililbooks.domain.auth.kakao.service.AuthKakaoService;
import com.example.ililbooks.global.dto.response.Response;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.example.ililbooks.config.util.CookieUtil.addRefreshTokenCookie;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth/kakao")
public class AuthKakaoController {

    private final AuthKakaoService authkakaoService;

    @GetMapping("/token")
    public Response<AuthKakaoTokenResponse> signInWithKakao(@RequestParam String code,
                                                            HttpServletResponse httpServletResponse) {
        AuthKakaoTokenResponse authKakaoTokenResponse = authkakaoService.signInWithKakao(code);
        addRefreshTokenCookie(httpServletResponse, authKakaoTokenResponse.refreshToken());
        return Response.of(authKakaoTokenResponse);
    }
}
