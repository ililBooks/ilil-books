package com.example.ililbooks.client.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoApiResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("expires_in")
        Long expiresIn,

        @JsonProperty("refresh_token_expires_in")
        Long refreshTokenExpiresIn,

        @JsonProperty("token_type")
        String tokenType
        ) {
}

