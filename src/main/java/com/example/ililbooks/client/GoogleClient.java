package com.example.ililbooks.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class GoogleClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${client.google.client-id}")
    private String clientId;

    @Value("${client.google.redirect-uri}")
    private String redirectUri;

    public GoogleClient(RestClient.Builder builder, ObjectMapper objectMapper) {
        this.restClient = builder.build();
        this.objectMapper = objectMapper;
    }

    public URI createAuthorizationUrl() {
        return buildGoogleApiUri();
    }

    /*
    https://accounts.google.com/o/oauth2/v2/auth
    ?client_id=399847506721-51v1rfjq6d308bap8j6bu21g4122i4uq.apps.googleusercontent.com
    &redirect_uri=http://localhost:8080/login/oauth2/code/google
    &response_type=code
    &scope=email profile
     */
    private URI buildGoogleApiUri() {
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
}
