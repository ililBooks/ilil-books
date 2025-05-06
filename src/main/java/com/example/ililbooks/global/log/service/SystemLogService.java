package com.example.ililbooks.global.log.service;

import com.example.ililbooks.global.log.collector.LogCollector;
import com.example.ililbooks.global.log.dto.request.LogRequest;
import com.example.ililbooks.global.log.dto.response.LogResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemLogService {

    private final LogCollector logCollector;

    public void saveRequestLog(LogRequest logRequest) {
        logCollector.collectRequestLog(logRequest);
    }

    public void saveResponseLog(LogResponse logResponse) {
        logCollector.collectResponseLog(logResponse);
    }
}