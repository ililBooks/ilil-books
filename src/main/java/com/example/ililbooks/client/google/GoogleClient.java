package com.example.ililbooks.client.google;

import com.example.ililbooks.client.google.dto.GoogleApiProfileResponse;
import com.example.ililbooks.client.google.dto.GoogleApiResponse;
import com.example.ililbooks.client.naver.dto.NaverApiProfileResponse;
import com.example.ililbooks.client.naver.dto.NaverApiProfileWrapper;
import com.example.ililbooks.client.naver.dto.NaverApiResponse;
import com.example.ililbooks.global.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Component
public class GoogleClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${client.google.client-id}")
    private String clientId;

    @Value("${client.google.client-secret}")
    private String clientSecret;

    @Value("${client.google.redirect-uri}")
    private String redirectUri;

    public GoogleClient(RestClient.Builder builder, ObjectMapper objectMapper) {
        this.restClient = builder.build();
        this.objectMapper = objectMapper;
    }

    public URI createAuthorizationUrl() {
        return buildAuthApiUri();
    }

    public GoogleApiResponse issueToken(String code) {
        MultiValueMap<String, String> body = buildAccessTokenApiBody(code);
        URI uri = buildAccessTokenApiUri();

        return findResponseBody(uri, body);
    }

    /*
    https://accounts.google.com/o/oauth2/v2/auth
    ?client_id=399847506721-51v1rfjq6d308bap8j6bu21g4122i4uq.apps.googleusercontent.com
    &redirect_uri=http://localhost:8080/login/oauth2/code/google
    &response_type=code
    &scope=email profile
     */
    private URI buildAuthApiUri() {
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

    private URI buildAccessTokenApiUri() {
        return UriComponentsBuilder
                .fromUriString("https://oauth2.googleapis.com/token")
                .encode()
                .build()
                .toUri();
    }

    /*
    client_id=...
    &client_secret=...
    &code=4/0AbUR...
    &redirect_uri=http://localhost:8080/login/oauth2/code/google
    &grant_type=authorization_code
     */
    private MultiValueMap<String, String> buildAccessTokenApiBody(String code) {
        MultiValueMap<String, String> formBody = new LinkedMultiValueMap<>();
        formBody.add("client_id", clientId);
        formBody.add("client_secret", clientSecret);
        formBody.add("code", code);
        formBody.add("redirect_uri", redirectUri);
        formBody.add("grant_type", "authorization_code");

        return formBody;
    }

    private URI buildUserProfileApiUri() {
        return UriComponentsBuilder
                .fromUriString("https://www.googleapis.com/userinfo/v2/me")
                .encode()
                .build()
                .toUri();
    }

    private GoogleApiResponse findResponseBody(URI uri, MultiValueMap<String, String> body) {
        ResponseEntity<String> responseEntity = restClient.post()
                .uri(uri)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(body)
                .retrieve()
                .toEntity(String.class);

        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            throw new RuntimeException(GOOGLE_API_RESPONSE_FAILED.getMessage());
        }

        String responseBody = responseEntity.getBody();

        try {
            return objectMapper.readValue(responseBody, GoogleApiResponse.class);

        } catch (Exception e) {
            throw new RuntimeException(GOOGLE_PASING_FAILED.getMessage(), e);
        }
    }

    public GoogleApiProfileResponse findProfile(String accessToken) {
        URI uri = buildUserProfileApiUri();

        ResponseEntity<String> responseEntity = restClient.get()
                .uri(uri)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken)) // 토큰 추가
                .retrieve()
                .toEntity(String.class);

        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            throw new RuntimeException(GOOGLE_API_RESPONSE_FAILED.getMessage());
        }

        String responseBody = responseEntity.getBody();

        try {

            //json 형태의 데이터 파싱
            GoogleApiProfileResponse googleApiProfileResponse = objectMapper.readValue(responseBody, GoogleApiProfileResponse.class);

            //검색된 프로필이 없는 경우
            if (ObjectUtils.isEmpty(googleApiProfileResponse)) {
                throw new NotFoundException(NOT_FOUND_PROFILE.getMessage());
            }

            return googleApiProfileResponse;

        } catch (Exception e) {
            throw new RuntimeException(NAVER_PASING_FAILED.getMessage(), e);
        }
    }
}
