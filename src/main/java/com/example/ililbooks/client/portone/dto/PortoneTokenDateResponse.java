package com.example.ililbooks.client.portone.dto;

public record PortoneTokenDateResponse(
        String access_token,
        long expired_at,
        long now
) {}
