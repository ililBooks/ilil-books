package com.example.ililbooks.global.log.dto.response;

import com.example.ililbooks.global.log.entity.SystemLog;
import lombok.Builder;

import java.time.Instant;

public record SystemLogResponse(
        Long id,
        String traceId,
        String actionType,
        String method,
        String url,
        String description,
        String ipAddress,
        String userAgent,
        Instant createdAt,
        Long userId,
        String errorCode,
        String errorMessage
) {

    @Builder
    public SystemLogResponse {

    }

    public static SystemLogResponse of(SystemLog log) {
        return SystemLogResponse.builder()
                .id(log.getId())
                .traceId(log.getTraceId())
                .actionType(log.getActionType())
                .method(log.getMethod())
                .url(log.getUrl())
                .description(log.getDescription())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .createdAt(log.getCreatedAt())
                .userId(log.getUsers() != null ? log.getUsers().getId() : null)
                .errorCode(log.getErrorCode())
                .errorMessage(log.getErrorMessage())
                .build();
    }
}
