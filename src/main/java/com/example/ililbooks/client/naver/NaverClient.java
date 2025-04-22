package com.example.ililbooks.client.naver;

import com.example.ililbooks.client.naver.dto.NaverApiProfileResponse;
import com.example.ililbooks.client.naver.dto.NaverApiProfileWrapper;
import com.example.ililbooks.client.naver.dto.NaverApiResponse;
import com.example.ililbooks.global.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
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
    private final HttpSession httpSession;

    @Value("${client.naver.client-id}")
    private String clientId;

    @Value("${client.naver.redirect-uri}")
    private String redirectUri;

    @Value("${client.naver.client-secret}")
    private String clientSecret;

    public NaverClient(WebClient.Builder builder, ObjectMapper objectMapper, HttpSession httpSession) {
        this.webClient = builder.build();
        this.objectMapper = objectMapper;
        this.httpSession = httpSession;
    }

    public URI getRedirectUrl() {
        return buildApiUri();
    }

    public NaverApiResponse issueToken(String code, String state) {
        URI uri = buildAccessTokenApiUri();

        try {
            return webClient.post()
                    .uri(uri)
                    .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                            .with("client_id", clientId)
                            .with("client_secret", clientSecret)
                            .with("code", code)
                            .with("state", state)
                    )
                    .retrieve()
                    .bodyToMono(NaverApiResponse.class)
                    .block();

        } catch (Exception e) {
            throw new RuntimeException(NAVER_PASING_FAILED.getMessage(), e);
        }
    }

    public NaverApiProfileResponse requestProfile(String accessToken) {
        URI uri = buildUserProfileApiUri();

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
            if (profile == null) {
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
    private URI buildApiUri() {

        //고유의 UUID 생성
        String state = String.valueOf(UUID.randomUUID());

        //세션에 저장
        httpSession.setAttribute("oauth_state", state);

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

    private URI buildAccessTokenApiUri() {

        return UriComponentsBuilder
                .fromUriString("https://nid.naver.com/oauth2.0/token")
                .encode()
                .build()
                .toUri();
    }

    private URI buildUserProfileApiUri() {
        return UriComponentsBuilder
                .fromUriString("https://openapi.naver.com/v1/nid/me")
                .encode()
                .build()
                .toUri();
    }
}
