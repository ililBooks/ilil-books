package com.example.ililbooks.global.log.dto.request;

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
) {}