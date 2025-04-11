package com.example.ililbooks.domain.auth.service;

import com.example.ililbooks.client.GoogleClient;
import com.example.ililbooks.client.dto.NaverApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class AuthGoogleService {

    private final GoogleClient googleClient;

    /* 로그인 인증 요청 */
    public URI createAuthorizationUrl() {
        return googleClient.createAuthorizationUrl();
    }

    public NaverApiResponse requestToken(String code, String state) {
    }
}
