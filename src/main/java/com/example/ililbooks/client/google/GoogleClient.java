package com.example.ililbooks.client.google;

import com.example.ililbooks.client.google.dto.GoogleApiProfileResponse;
import com.example.ililbooks.client.google.dto.GoogleApiResponse;
import com.example.ililbooks.global.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Component
public class GoogleClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${client.google.client-id}")
    private String clientId;

    @Value("${client.google.client-secret}")
    private String clientSecret;

    @Value("${client.google.redirect-uri}")
    private String redirectUri;

    public GoogleClient(WebClient.Builder builder, ObjectMapper objectMapper) {
        this.webClient = builder.build();
        this.objectMapper = objectMapper;
    }

    /* 인증 API 요청 */
    public URI createAuthorizationUrl() {
        return UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "email profile")
                .encode()
                .build()
                .toUri();
    }

    /* 토큰 발급 */
    public GoogleApiResponse issueToken(String code) {

        MultiValueMap<String, String> formBody = new LinkedMultiValueMap<>();

        formBody.add("client_id", clientId);
        formBody.add("client_secret", clientSecret);
        formBody.add("code", code);
        formBody.add("redirect_uri", redirectUri);
        formBody.add("grant_type", "authorization_code");

        URI uri = UriComponentsBuilder
                .fromUriString("https://oauth2.googleapis.com/token")
                .encode()
                .build()
                .toUri();

        String responseBody = webClient.post()
                .uri(uri)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(formBody)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        res -> Mono.error(new RuntimeException(GOOGLE_API_RESPONSE_FAILED.getMessage())))
                .bodyToMono(String.class)
                .block();
        try {
            return objectMapper.readValue(responseBody, GoogleApiResponse.class);

        } catch (Exception e) {
            throw new RuntimeException(GOOGLE_PASING_FAILED.getMessage(), e);
        }
    }

    /* 유저 프로필 조회 */
    public GoogleApiProfileResponse requestProfile(String accessToken) {

        URI uri = UriComponentsBuilder
                .fromUriString("https://www.googleapis.com/userinfo/v2/me")
                .encode()
                .build()
                .toUri();

        String responseBody = webClient.get()
                .uri(uri)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        res -> Mono.error(new RuntimeException(GOOGLE_API_RESPONSE_FAILED.getMessage())))
                .bodyToMono(String.class)
                .block();

        try {
            GoogleApiProfileResponse googleApiProfileResponse = objectMapper.readValue(responseBody, GoogleApiProfileResponse.class);

            if (googleApiProfileResponse == null) {
                throw new NotFoundException(NOT_FOUND_PROFILE.getMessage());
            }

            return googleApiProfileResponse;
        } catch (Exception e) {
            throw new RuntimeException(GOOGLE_PASING_FAILED.getMessage(), e);
        }
    }
}
