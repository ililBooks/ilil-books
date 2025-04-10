package com.example.ililbooks.domain.auth.dto.response;

import lombok.Builder;

public record AuthAccessTokenResponse(String accessToken) {

    @Builder
    public AuthAccessTokenResponse {
    }

    public static AuthAccessTokenResponse of(String accessToken) {
        return AuthAccessTokenResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}