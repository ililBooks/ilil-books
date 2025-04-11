package com.example.ililbooks.domain.auth.service;

import com.example.ililbooks.client.google.GoogleClient;
import com.example.ililbooks.client.google.dto.GoogleApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class AuthGoogleService {

    private final GoogleClient googleClient;

    /* 로그인 인증 요청 */
    @Transactional(readOnly = true)
    public URI createAuthorizationUrl() {
        return googleClient.createAuthorizationUrl();
    }

    @Transactional
    public GoogleApiResponse requestToken(String code) {
        return googleClient.issueToken(code);
    }
}
