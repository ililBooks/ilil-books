package com.example.ililbooks.domain.auth.dto.response;

import lombok.Builder;

public record AuthTokensResponse(String accessToken, String refreshToken) {

    @Builder
    public AuthTokensResponse {
    }

    public static AuthTokensResponse of(String accessToken, String refreshToken) {
        return AuthTokensResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
