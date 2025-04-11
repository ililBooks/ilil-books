package com.example.ililbooks.domain.auth.service;

import com.example.ililbooks.client.NaverClient;
import com.example.ililbooks.client.dto.NaverProfileResponse;
import com.example.ililbooks.client.dto.NaverResponse;
import com.example.ililbooks.domain.auth.dto.request.NaverReqeust;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class NaverService {

    private final NaverClient naverClient;

    @Transactional
    public URI createAuthorizationUrl() {
        return naverClient.createAuthorizationUrl();
    }

    @Transactional
    public NaverResponse requestNaverAccessToken(String code, String state) {
        return naverClient.findAccessToken(code, state);
    }

    @Transactional
    public NaverProfileResponse[] findProfile(NaverReqeust naverReqeust) {
        return naverClient.findProfile(naverReqeust.accessToken());
    }
}
