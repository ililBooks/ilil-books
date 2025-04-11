package com.example.ililbooks.domain.auth.controller;

import com.example.ililbooks.client.dto.NaverProfileResponse;
import com.example.ililbooks.client.dto.NaverResponse;
import com.example.ililbooks.domain.auth.dto.request.NaverReqeust;
import com.example.ililbooks.domain.auth.service.NaverService;
import com.example.ililbooks.global.dto.response.Response;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/naver")
@Tag(name = "Naver", description = "Naver 로그인 및 회원가입, 토큰 재발급과 관련된 API")
public class NaverAuthController {
    private final NaverService naverService;

    /**
     * 네이버 로그인 인증 요청을 위한 API 입니다.
     */
    @PostMapping("/sign-in")
    public Response<URI> createAuthorizationUrl () {
        return Response.of(naverService.createAuthorizationUrl());
    }

    /**
     * 로그인 인증 성공 후 접근 토큰 발급 요철을 하는 API 입니다.
     */
    @PostMapping("/token")
    public Response<NaverResponse> requestNaverAccessToken(
            @RequestParam String code,
            @RequestParam String state
    ) {
        return Response.of(naverService.requestNaverAccessToken(code, state));
    }

    /**
     * 접근 토큰 발급 후 해당 토큰으로 프로필을 조회하는 API 입니다.
     */
    @PostMapping("/profile")
    public Response<NaverProfileResponse[]> findProfile(
            @RequestBody NaverReqeust naverReqeust
    ) {
        return Response.of(naverService.findProfile(naverReqeust));
    }
}
