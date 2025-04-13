package com.example.ililbooks.domain.auth.kakao.dto.response;

import org.springframework.security.oauth2.core.user.OAuth2User;

public record KakaoOAuth2UserResponse(OAuth2User oAuth2User) {
}
