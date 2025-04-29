package com.example.ililbooks.global.log.collector;

import com.example.ililbooks.global.log.dto.request.LogRequest;
import com.example.ililbooks.global.log.dto.response.LogResponse;

public interface LogCollector {

    void collectRequestLog(LogRequest logRequest);

    void collectResponseLog(LogResponse logResponse);
}