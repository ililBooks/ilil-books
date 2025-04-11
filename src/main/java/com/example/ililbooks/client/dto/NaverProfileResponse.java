package com.example.ililbooks.client.dto;

public record NaverProfileResponse(
        String nickname,

        String name,

        String email,

        String mobile
) {
}
