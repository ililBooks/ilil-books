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
    }

    @Override
    public void collectResponseLog(LogResponse response) {
    }
}
