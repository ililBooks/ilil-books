package com.example.ililbooks.client.naver;

import com.example.ililbooks.client.naver.dto.NaverApiProfileResponse;
import com.example.ililbooks.client.naver.dto.NaverApiProfileWrapper;
import com.example.ililbooks.client.naver.dto.NaverApiResponse;
import com.example.ililbooks.global.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Component
public class NaverClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${client.naver.client-id}")
    private String clientId;

    @Value("${client.naver.redirect-uri}")
    private String redirectUri;

    @Value("${client.naver.client-secret}")
    private String clientSecret;

    public NaverClient(WebClient.Builder builder, ObjectMapper objectMapper) {
        this.webClient = builder.build();
        this.objectMapper = objectMapper;
    }

    public URI getRedirectUrl() {
        return buildNaverApiUri();
    }

    public NaverApiResponse issueToken(String code, String state) {
        URI uri = buildNaverAccessTokenApiUri(code, state);

        return findResponseBody(uri);
    }

    public NaverApiProfileResponse findProfile(String accessToken) {
        URI uri = buildNaverUserProfileApiUri();

        String responseBody = webClient.get()
                .uri(uri)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        res -> Mono.error(new RuntimeException(NAVER_API_RESPONSE_FAILED.getMessage())))
                .bodyToMono(String.class)
                .block();

        try {

            //json 형태의 데이터 파싱
            NaverApiProfileWrapper naverProfile = objectMapper.readValue(responseBody, NaverApiProfileWrapper.class);
            NaverApiProfileResponse profile = naverProfile.response();

            //검색된 프로필이 없는 경우
            if (ObjectUtils.isEmpty(profile)) {
                throw new NotFoundException(NOT_FOUND_PROFILE.getMessage());
            }

            return profile;

        } catch (Exception e) {
            throw new RuntimeException(NAVER_PASING_FAILED.getMessage(), e);
        }
    }

    /**
     * response_type: 인증 과정에 대한 내부 구분 값 (반드시 code)로 전송
     * client_id: 등록된 Client ID
     * redirect_uri: callback URL
     * state:  위조 공격 방지를 위한 상태값
     */
    private URI buildNaverApiUri() {

        //고유의 UUID 생성
        String state = String.valueOf(UUID.randomUUID());

        return UriComponentsBuilder
                .fromUriString("https://nid.naver.com/oauth2.0/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state",state)
                .encode()
                .build()
                .toUri();
    }

    /**
     *
     * @param code: redirect_uri를 통해 얻은 내부 구분값
     * @param state: redirect_uri를 통해 얻은 상태 값
     *
     * grant_type: authorization_code(발급)
     */
    private URI buildNaverAccessTokenApiUri(String code, String state) {

        return UriComponentsBuilder
                .fromUriString("https://nid.naver.com/oauth2.0/token")
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("code", code)
                .queryParam("state", state)
                .encode()
                .build()
                .toUri();
    }

    private URI buildNaverUserProfileApiUri() {
        return UriComponentsBuilder
                .fromUriString("https://openapi.naver.com/v1/nid/me")
                .encode()
                .build()
                .toUri();
    }

    private NaverApiResponse findResponseBody(URI uri) {
        String responseBody = webClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        res -> Mono.error(new RuntimeException(NAVER_API_RESPONSE_FAILED.getMessage())))
                .bodyToMono(String.class)
                .block();

        try {
            return objectMapper.readValue(responseBody, NaverApiResponse.class);

        } catch (Exception e) {
            throw new RuntimeException(NAVER_PASING_FAILED.getMessage(), e);
        }
    }
}
