package com.example.ililbooks.client.portone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Open API 응답의 필드명(access_token)은
 * java 네이밍에 맞지 않기 때문에
 * 이를 매핑하기 위해 각 필드에 @JsonProperty 어노테이션을 사용한다.
 *
 * @param accessToken
 */
public record PortoneTokenResponse(
        @JsonProperty("access_token")
        String accessToken
) {
}
