package com.example.ililbooks.client.google.dto;

import lombok.Builder;

public record GoogleApiProfileResponse(
        String email,
        String name
){
    @Builder
    public GoogleApiProfileResponse {
    }
}
