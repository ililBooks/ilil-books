package com.example.ililbooks.domain.auth.kakao.controller;

import com.example.ililbooks.domain.auth.kakao.service.AuthkakaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth/kakao")
public class AuthKakaoController {

    private final AuthkakaoService authkakaoService;

    @PostMapping
    public ResponseEntity<?> signinWithKakao(@RequestParam String code) {
        return authkakaoService.signinWithKakao(code);
    }
}
