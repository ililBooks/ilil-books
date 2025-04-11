package com.example.ililbooks.client.google.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Open API 응답의 필드명(access_token, id_token, token_type, expires_in, scope)은
 * java 네이밍에 맞지 않기 때문에
 * 이를 매핑하기 위해 각 필드에 @JsonProperty 어노테이션을 사용한다.
 *
 * @param accessToken
 * @param idToken
 * @param tokenType
 * @param expiresIn
 * @param scope
 */
public record GoogleApiResponse (
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("id_token")
        String idToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        Integer expiresIn,

        @JsonProperty("scope")
        String scope
        ){
}