package com.example.ililbooks.domain.auth.kakao.hadler;

import com.example.ililbooks.config.util.JwtUtil;
import com.example.ililbooks.domain.auth.kakao.OAuth2User.KakaoOAuth2User;
import com.example.ililbooks.domain.auth.service.TokenService;
import com.example.ililbooks.domain.user.entity.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/*
* Spring Security 에서 OAuth2 로그인 성공 후 호출되는 커스텀 로직을 구현
* OAuth2 인증에 성공 -> onAuthenticationSuccess() 메서드 자동 호출
* */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        KakaoOAuth2User kakaoOAuth2User = (KakaoOAuth2User) authentication.getPrincipal();
        Users user = kakaoOAuth2User.getUser();

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getNickname(), user.getUserRole());
        String refreshToken = tokenService.createRefreshToken(user);

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        Map<String, String> tokens = Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );

        new ObjectMapper().writeValue(response.getWriter(), tokens);
    }
}