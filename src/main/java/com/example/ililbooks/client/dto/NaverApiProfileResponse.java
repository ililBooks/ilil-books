package com.example.ililbooks.client.dto;

public record NaverApiProfileResponse(
        String nickname,

        String name,

        String email,

        String mobile
) {
}
