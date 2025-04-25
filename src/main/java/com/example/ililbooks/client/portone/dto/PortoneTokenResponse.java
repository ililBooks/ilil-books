package com.example.ililbooks.client.portone.dto;

public record PortoneTokenResponse(
        int code,
        String message,
        PortoneTokenDateResponse response
) {}
