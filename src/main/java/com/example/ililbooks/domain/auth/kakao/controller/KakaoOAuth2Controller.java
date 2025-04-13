package com.example.ililbooks.domain.auth.kakao.controller;

import com.example.ililbooks.domain.auth.kakao.dto.request.KakaoOAuth2TokenRequest;
import com.example.ililbooks.domain.auth.kakao.dto.request.KakaoOAuth2UserRequest;
import com.example.ililbooks.domain.auth.kakao.dto.response.KakaoOAuth2TokenResponse;
import com.example.ililbooks.domain.auth.kakao.dto.response.KakaoOAuth2UserResponse;
import com.example.ililbooks.domain.auth.kakao.service.KakaoOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/auth/kakao")
public class KakaoOAuth2Controller {

    private final KakaoOAuth2UserService kakaoOAuth2UserService;

    /* 사용자 정보로 회원 정보 찾기 또는 없을 경우 정보 local DB 저장 메서드 */
    @PostMapping
    public ResponseEntity<KakaoOAuth2UserResponse> signIn(@RequestBody KakaoOAuth2UserRequest kakaoOAuth2UserRequest) {
        return ResponseEntity.ok(new KakaoOAuth2UserResponse(kakaoOAuth2UserService.loadUser(kakaoOAuth2UserRequest)));
    }

    /* refresh token 발급 메서드 */
    @PostMapping("/token/refresh")
    public ResponseEntity<KakaoOAuth2TokenResponse> refreshToken(@RequestBody KakaoOAuth2TokenRequest kakaoOAuth2TokenRequest) {
        return ResponseEntity.ok(kakaoOAuth2UserService.refreshToken(kakaoOAuth2TokenRequest));
    }
}
