package com.example.ililbooks.domain.auth.kakao.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * 프로젝트 내에서 사용하는 camel case 와
 * kakao client 에서 사용하는 snake case 에 따른 차이에서 parsing 문제가 생겨
 * @JsonProperty 사용해 프로퍼티 이름 명시
 * */
public record AuthKakaoTokenResponse (
        String signupUri,

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken
) {
}
