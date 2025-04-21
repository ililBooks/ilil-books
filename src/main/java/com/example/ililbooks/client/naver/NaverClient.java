package com.example.ililbooks.client.naver;

import com.example.ililbooks.client.naver.dto.NaverApiProfileResponse;
import com.example.ililbooks.client.naver.dto.NaverApiProfileWrapper;
import com.example.ililbooks.client.naver.dto.NaverApiResponse;
import com.example.ililbooks.global.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
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
        return buildApiUri();
    }

    public NaverApiResponse issueToken(String code, String state) {
        URI uri = buildAccessTokenApiUri(code, state);

        return findResponseBody(uri);
    }

    public NaverApiProfileResponse findProfile(String accessToken) {
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
    private URI buildApiUri() {

        //고유의 UUID 생성
        //TODO 얘도 매 요청마다 새로 생성해줍니다. 근데 이거 어디에도 저장이 되지 않네요
        // 요거는 CSRF 공격 방지때문에 있는건데, 요런 경우에는 콜백 요청시 공격자가 대충 만든 URL인지 판단할 수가 없습니다.
        // 따라서 지금 코드로는 state가 있는이유가 없어요 세션에 저장해놓는 경우가 많아요
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
    private URI buildAccessTokenApiUri(String code, String state) {

        return UriComponentsBuilder
                .fromUriString("https://nid.naver.com/oauth2.0/token")
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId) //TODO 얘네 body로 보내세요 +grant_type,
                .queryParam("client_secret", clientSecret)
                .queryParam("code", code)
                .queryParam("state", state)
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
