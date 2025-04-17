package com.example.ililbooks.domain.auth.kakao.service;

import com.example.ililbooks.client.kakao.KakaoClient;
import com.example.ililbooks.domain.auth.kakao.dto.response.AuthKakaoResponse;
import com.example.ililbooks.domain.auth.kakao.dto.response.AuthKakaoTokenResponse;
import com.example.ililbooks.domain.auth.service.TokenService;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthkakaoService {

    private final UserService userService;
    private final TokenService tokenService;
    private final KakaoClient kakaoClient;

    public ResponseEntity<?> signinWithKakao(String code) {
        // 인가 토큰 받기
        AuthKakaoTokenResponse tokenResponse = kakaoClient.requestToken(code);
        System.out.println(tokenResponse.toString() + tokenResponse.accessToken());
        // 사용자 정보 조회
        AuthKakaoResponse userInfo = kakaoClient.requestUserInfo(tokenResponse.accessToken());

        // 사용자 검증 후 커카오 회원 가입 redirect
        if (userInfo.kakaoAccount().email().isBlank() || userInfo.kakaoAccount().profile().nickname().isBlank()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, kakaoClient.getKakaoSignupUri())
                    .build();
        }

        // 사용자 정보 프로젝트에 저장 또는 있을 경우 반환
        Users user = userService.findByEmailOrGet(userInfo.kakaoAccount().email(), userInfo.kakaoAccount().profile().nickname());
        
        // 토큰 발급
        String accessToken = tokenService.createAccessToken(user);
        String refreshToken = tokenService.createRefreshToken(user);

        return ResponseEntity.status(HttpStatus.FOUND)
                .body(new AuthKakaoTokenResponse(accessToken, refreshToken));
    }
}
