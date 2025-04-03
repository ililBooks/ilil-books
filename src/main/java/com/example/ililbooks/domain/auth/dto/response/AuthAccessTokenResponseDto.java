package com.example.ililbooks.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthAccessTokenResponseDto {

    private final String accessToken;

}