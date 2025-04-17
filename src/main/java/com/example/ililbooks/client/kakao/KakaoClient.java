package com.example.ililbooks.client.kakao;

import com.example.ililbooks.domain.auth.kakao.dto.response.AuthKakaoResponse;
import com.example.ililbooks.domain.auth.kakao.dto.response.AuthKakaoTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
public class KakaoClient {

    private final WebClient webClient;
    private final String clientId;
    private final String redirectUri;
    private final String clientSecret;

    public KakaoClient(
            @Value("${client.kakao.client-id}") String clientId,
            @Value("${client.kakao.redirect-uri}") String redirectUri,
            @Value("${client.kakao.client-secret}") String clientSecret
    ) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.clientSecret = clientSecret;

        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    public String getKakaoSignupUri() {
        return UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com/oauth/authorize") // 인가 코드 받기
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("prompt", "create") // 카카오 회원 가입
                .build()
                .toUriString();
    }

    public AuthKakaoTokenResponse requestToken(String code) {
        return webClient.post()
                .uri("https://kauth.kakao.com/oauth/token") // 토큰 받기
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", clientId)
                        .with("redirect_uri", redirectUri)
                        .with("client_secret", clientSecret)
                        .with("code", code))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class).flatMap(errorBody ->
                                Mono.error(new RuntimeException("Kakao API ACCESS 토큰 응답: " + errorBody))
                        )
                )
                .bodyToMono(AuthKakaoTokenResponse.class)
                .block(); // 동기 처리
    }

    public AuthKakaoResponse requestUserInfo(String accessToken) {
        return webClient.mutate()
                .baseUrl("https://kapi.kakao.com")
                .build()
                .get()
                .uri("/v2/user/me") // 사용자 정보 조회 uri
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class).flatMap(errorBody ->
                                Mono.error(new RuntimeException("Kakao API 사용자 정보 조회 응답: " + errorBody))
                        )
                )
                .bodyToMono(AuthKakaoResponse.class)
                .block(); // 동기 처리
    }
}