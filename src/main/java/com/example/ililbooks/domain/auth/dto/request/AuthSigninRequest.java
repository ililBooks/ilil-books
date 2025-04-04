package com.example.ililbooks.domain.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AuthSigninRequest {

    private String email;
    private String password;

    @Builder
    public AuthSigninRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
