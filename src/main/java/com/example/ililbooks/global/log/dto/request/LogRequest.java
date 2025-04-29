package com.example.ililbooks.global.log.dto.request;

import lombok.Builder;

import java.time.Instant;

public record LogRequest(
        String traceId,
        String method,
        String url,
        Long userId,
        Instant timestamp,
        Object body,
        String actionType,
        String ip,
        String userAgent,
        String errorCode,
        String description
) {

    @Builder
    public LogRequest {
    }

    public static LogRequest of(String traceId, String method, String url, Long userId, Instant timestamp, Object body, String actionType, String ip, String userAgent, String errorCode, String description) {
        return LogRequest.builder()
                .traceId(traceId)
                .method(method)
                .url(url)
                .userId(userId)
                .timestamp(timestamp)
                .body(body)
                .actionType(actionType)
                .ip(ip)
                .userAgent(userAgent)
                .errorCode(errorCode)
                .description(description)
                .build();
    }
}