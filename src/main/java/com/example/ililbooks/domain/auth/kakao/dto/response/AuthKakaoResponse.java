package com.example.ililbooks.domain.auth.kakao.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthKakaoResponse(
        Long id,

        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {
    public record KakaoAccount(
            String email,
            Profile profile,

            @JsonProperty("has_email")
            boolean hasEmail,

            @JsonProperty("is_email_valid")
            boolean isEmailValid,

            @JsonProperty("is_email_verified")
            boolean isEmailVerified
    ) { }

    public record Profile(
            String nickname,

            @JsonProperty("profile_image_url")
            String profileImageUrl,

            @JsonProperty("thumbnail_image_url")
            String thumbnailImageUrl
    ) { }
}