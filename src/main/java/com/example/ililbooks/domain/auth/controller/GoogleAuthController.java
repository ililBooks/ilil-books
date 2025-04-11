package com.example.ililbooks.domain.auth.controller;

import com.example.ililbooks.domain.auth.service.GoogleAuthService;
import com.example.ililbooks.global.dto.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/google")
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;

    /* 로그인 인증 요청 */
    @GetMapping("/sign-in")
    public Response<URI> createAuthorizationUrl() {
        return Response.of(googleAuthService.createAuthorizationUrl());
    }
}
