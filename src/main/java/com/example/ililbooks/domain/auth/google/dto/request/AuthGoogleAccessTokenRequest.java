package com.example.ililbooks.domain.auth.google.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "구글 Access Token 요청을 위한 DTO")
public record AuthGoogleAccessTokenRequest(

        @Schema(example = "ya29.a0AZYkNZhq_wYP_WN-YnyP19QSsgiW7OOjgT...")
        String accessToken
) {
        @Builder
        public AuthGoogleAccessTokenRequest {
        }
}