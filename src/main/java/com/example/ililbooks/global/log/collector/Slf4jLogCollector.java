package com.example.ililbooks.global.log.collector;

import com.example.ililbooks.global.log.dto.request.LogRequest;
import com.example.ililbooks.global.log.dto.response.LogResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Slf4jLogCollector implements LogCollector {

    @Override
    public void collectRequestLog(LogRequest request) {
        if (request != null) {
            log.info("[LOG-COLLECTOR][REQUEST] TraceId: {}, ActionType: {}, Method: {}, URL: {}, UserId: {}",
                    request.traceId(), request.actionType(), request.method(), request.url(), request.userId());
        } else {
            log.warn("[LOG-COLLECTOR][REQUEST] Received null request log.");
        }
    }

    @Override
    public void collectResponseLog(LogResponse response) {
        if (response != null) {
            log.info("[LOG-COLLECTOR][RESPONSE] TraceId: {}, Status: {}",
                    response.traceId(), response.status());
        } else {
            log.warn("[LOG-COLLECTOR][RESPONSE] Received null response log.");
        }
    }
}