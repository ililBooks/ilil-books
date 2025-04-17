package com.example.ililbooks.domain.auth.kakao.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthKakaoTokenResponse (
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken
) {
}
