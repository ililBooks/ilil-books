package com.example.ililbooks.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AuthAccessTokenResponse {

    private final String accessToken;

    @Builder
    private AuthAccessTokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public static AuthAccessTokenResponse ofDto(String accessToken) {
        return AuthAccessTokenResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}