package com.example.ililbooks.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AuthTokensResponse {

    private final String accessToken;
    private final String refreshToken;

    @Builder
    private AuthTokensResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static AuthTokensResponse ofDto(String accessToken, String refreshToken) {
        return AuthTokensResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
