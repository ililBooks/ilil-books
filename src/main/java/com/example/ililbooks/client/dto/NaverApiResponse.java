package com.example.ililbooks.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Open API 응답의 필드명(access_token, refresh_token, token_type, expires_in)은
 * java 네이밍에 맞지 않기 때문에
 * 이를 매핑하기 위해 각 필드에 @JsonProperty 어노테이션을 사용한다.
 *
 * @param accessToken
 * @param refreshToken
 * @param tokenType
 * @param expriesIn
 */
public record NaverApiResponse(
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
