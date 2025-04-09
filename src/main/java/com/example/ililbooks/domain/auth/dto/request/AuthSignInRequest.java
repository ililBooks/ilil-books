package com.example.ililbooks.domain.auth.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AuthSignInRequest {

    private String email;
    private String password;

    @Builder
    public AuthSignInRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
