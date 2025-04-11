package com.example.ililbooks.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverProfileResponse(
        String nickname,

        String name,

        String email,

        String moblie

) {
}
