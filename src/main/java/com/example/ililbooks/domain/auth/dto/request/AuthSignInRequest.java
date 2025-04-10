package com.example.ililbooks.domain.auth.dto.request;

import lombok.Builder;

public record AuthSignInRequest(String email, String password) {

    @Builder
    public AuthSignInRequest {
    }
}
