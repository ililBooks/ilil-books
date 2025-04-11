package com.example.ililbooks.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        Integer expriesIn
) {
}
