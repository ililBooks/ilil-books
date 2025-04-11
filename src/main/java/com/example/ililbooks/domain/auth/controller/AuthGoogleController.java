package com.example.ililbooks.domain.auth.controller;

import com.example.ililbooks.client.dto.GoogleApiResponse;
import com.example.ililbooks.domain.auth.service.AuthGoogleService;
import com.example.ililbooks.global.dto.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/google")
public class AuthGoogleController {

    private final AuthGoogleService authGoogleService;

    /* 로그인 인증 요청 */
    @GetMapping("/sign-in")
    public Response<URI> createAuthorizationUrl() {
        return Response.of(authGoogleService.createAuthorizationUrl());
    }

    @PostMapping("/token")
    public Response<GoogleApiResponse> requestToken(
            @RequestParam String code,
            @RequestParam String state
    ) {
        return Response.of(authGoogleService.requestToken(code, state));
    }
}
