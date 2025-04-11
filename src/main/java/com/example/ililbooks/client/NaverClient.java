package com.example.ililbooks.client;

import com.example.ililbooks.client.dto.*;
import com.example.ililbooks.global.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Component
public class NaverClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${client.naver.client-id}")
    private String clientId;

    @Value("${client.naver.redirect-uri}")
    private String redirectUri;

    @Value("${client.naver.client-secret}")
    private String clientSecret;

    public NaverClient(RestClient.Builder builder, ObjectMapper objectMapper) {
        this.restClient = builder.build();
        this.objectMapper = objectMapper;
    }

    public URI getNaverLoginRedirectUrl() {
        return buildNaverApiUri();
    }

    public NaverResponse findToken(String code, String state) {
        URI uri = tokenNaverApiUri(code, state);

        ResponseEntity<String> responseEntity = getResponseEntity(uri);

        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            throw new RuntimeException(NAVER_API_RESPONSE_FAILED.getMessage());
        }

        String responseBody = responseEntity.getBody();

        try {
            return objectMapper.readValue(responseBody, NaverResponse.class);

        } catch (Exception e) {
            throw new RuntimeException(NAVER_PASING_FAILED.getMessage(), e);
        }
    }

    public NaverResponse findRefreshToken(String refreshToken) {
        URI uri = refreshTokenNaverApiUri(refreshToken);

        ResponseEntity<String> responseEntity = getResponseEntity(uri);

        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            throw new RuntimeException(NAVER_API_RESPONSE_FAILED.getMessage());
        }

        String responseBody = responseEntity.getBody();

        try {
            return objectMapper.readValue(responseBody, NaverResponse.class);

        } catch (Exception e) {
            throw new RuntimeException(NAVER_PASING_FAILED.getMessage(), e);
        }
    }

    public NaverProfileResponse findProfile(String accessToken) {
        URI uri = profileNaverApiUri();

        ResponseEntity<String> responseEntity = restClient.get()
                .uri(uri)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken)) // 토큰 추가
                .retrieve()
                .toEntity(String.class);

        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            throw new RuntimeException(NAVER_API_RESPONSE_FAILED.getMessage());
        }

        String responseBody = responseEntity.getBody();

        try {

            //json 형태의 데이터 파싱
            NaverProfileWrapper naverProfile = objectMapper.readValue(responseBody, NaverProfileWrapper.class);
            NaverProfileResponse profile = naverProfile.response();

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
    private URI tokenNaverApiUri(String code, String state) {

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

    private URI refreshTokenNaverApiUri(String refreshToken) {

        return UriComponentsBuilder
                .fromUriString("https://nid.naver.com/oauth2.0/token")
                .queryParam("grant_type", "refresh_token")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("refresh_token", refreshToken)
                .encode()
                .build()
                .toUri();
    }

    private URI profileNaverApiUri() {
        return UriComponentsBuilder
                .fromUriString("https://openapi.naver.com/v1/nid/me")
                .encode()
                .build()
                .toUri();
    }

    private ResponseEntity<String> getResponseEntity(URI uri) {
        return restClient.get()
                .uri(uri)
                .retrieve()
                .toEntity(String.class);
    }
}
