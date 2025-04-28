package com.example.ililbooks.global.log.dto.response;

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
) {}