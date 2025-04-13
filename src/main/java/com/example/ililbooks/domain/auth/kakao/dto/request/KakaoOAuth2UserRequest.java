package com.example.ililbooks.domain.auth.kakao.dto.request;

import lombok.Getter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.util.Map;
/*
* OAuth2UserRequest 를 상속해야 하므로 record 가 아닌 class 로 작성함
* */
@Getter
public class KakaoOAuth2UserRequest extends OAuth2UserRequest {
    public KakaoOAuth2UserRequest(ClientRegistration clientRegistration, OAuth2AccessToken accessToken) {
        super(clientRegistration, accessToken);
    }

    public KakaoOAuth2UserRequest(ClientRegistration clientRegistration, OAuth2AccessToken accessToken, Map<String, Object> additionalParameters) {
        super(clientRegistration, accessToken, additionalParameters);
    }
}
