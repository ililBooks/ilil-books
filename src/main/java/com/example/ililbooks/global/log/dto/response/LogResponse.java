package com.example.ililbooks.global.log.dto.response;

import lombok.Builder;

import java.time.Instant;

public record LogResponse(
        String traceId,
        String method,
        String url,
        Long userId,
        Instant timestamp,
        Object body,
        String actionType,
        String ip,
        String userAgent,
        String status,
        String errorCode
) {

    @Builder
    public LogResponse {
    }

    public static LogResponse of(String traceId, String method, String url, Long userId, Instant timestamp, Object body, String actionType, String ip, String userAgent, String status, String errorCode) {
        return LogResponse.builder()
                .traceId(traceId)
                .method(method)
                .url(url)
                .userId(userId)
                .timestamp(timestamp)
                .body(body)
                .actionType(actionType)
                .ip(ip)
                .userAgent(userAgent)
                .status(status)
                .errorCode(errorCode)
                .build();
    }
}