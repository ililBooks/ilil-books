package com.example.ililbooks.client.google;

import com.example.ililbooks.client.google.dto.GoogleApiProfileResponse;
import com.example.ililbooks.client.google.dto.GoogleApiResponse;
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
        return buildAuthApiUri();
    }

    /* 토큰 발급 */
    public GoogleApiResponse issueToken(String code) {
        MultiValueMap<String, String> body = buildAccessTokenApiBody(code);
        URI uri = buildAccessTokenApiUri();

        return findResponseBody(uri, body);
    }

    /* 유저 프로필 조회 */
    public GoogleApiProfileResponse findProfile(String accessToken) {
        URI uri = buildUserProfileApiUri();

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

            if (ObjectUtils.isEmpty(googleApiProfileResponse)) { //TODO googleApiProfileResponse얘는 record 객체에요, 객체가 Null인지 아닌지만 판단하면 됩니디ㅏ
                //만약에 new GoogleApiProfileResponse(null,null)이면 null인지 아닌지 애매해져요, 그렇기 때문에 이 오브젝트가 비어있나가 의미가없어요
                //googleApiProfileResponse == null 이런식으로 가는게 깔끔합니다 / list map.. 등등은 오브젝트 엠티로 판단하지만 , record클래스는 null체크가 더 용이함
                throw new NotFoundException(NOT_FOUND_PROFILE.getMessage());
            }

            return googleApiProfileResponse;
        } catch (Exception e) {
            throw new RuntimeException(NAVER_PASING_FAILED.getMessage(), e); //??? GOOGLE로 수정필요
        }
    }

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

    //TODO 요 네이밍은 HTTP 프로토콜 요청을 나타내지 않습니다.
    private GoogleApiResponse findResponseBody(URI uri, MultiValueMap<String, String> body) {

        String responseBody = webClient.post()
                .uri(uri)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(body)
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
}
