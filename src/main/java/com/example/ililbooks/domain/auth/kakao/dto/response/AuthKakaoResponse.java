package com.example.ililbooks.domain.auth.kakao.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 프로젝트 내에서 사용하는 camel case 와
 * kakao client 에서 사용하는 snake case 에 따른 차이에서 parsing 문제가 생겨
 * @JsonProperty 사용해 프로퍼티 이름 명시
 * */
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