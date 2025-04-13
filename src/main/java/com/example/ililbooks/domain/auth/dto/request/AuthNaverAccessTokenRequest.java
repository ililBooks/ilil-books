package com.example.ililbooks.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입을 위한 접근 토근 요청 DTO")
public record AuthNaverAccessTokenRequest(
        @Schema(description = "네이버 인증 요청 후 발급된 접근 토큰")
        String accessToken
){
}
