package com.example.ililbooks.global.log.collector;

import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.repository.UserRepository;
import com.example.ililbooks.global.log.dto.request.LogRequest;
import com.example.ililbooks.global.log.dto.response.LogResponse;
import com.example.ililbooks.global.log.entity.SystemLog;
import com.example.ililbooks.global.log.repository.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class DataBaseLogCollector implements LogCollector {

    private final SystemLogRepository systemLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void collectRequestLog(LogRequest logRequest) {
        try {
            Users user = findUserOrNull(logRequest.userId());

            SystemLog systemLog = SystemLog.of(
                    logRequest.traceId(),
                    user,
                    logRequest.actionType(),
                    logRequest.method(),
                    logRequest.url(),
                    logRequest.body() != null ? logRequest.body().toString() : null,
                    logRequest.ip(),
                    logRequest.userAgent()
            );

            systemLogRepository.save(systemLog);
        } catch (Exception e) {
            log.warn("[DB 로그] 요청 로그 저장 에러: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void collectResponseLog(LogResponse logResponse) {
        try {
            Users user = findUserOrNull(logResponse.userId());

            SystemLog systemLog = SystemLog.of(
                    logResponse.traceId(),
                    user,
                    logResponse.actionType(),
                    logResponse.method(),
                    logResponse.url(),
                    logResponse.body() != null ? logResponse.body().toString() : null,
                    logResponse.ip(),
                    logResponse.userAgent()
            );

            systemLogRepository.save(systemLog);
        } catch (Exception e) {
            log.warn("[DB 로그] 응답 로그 저장 에러: {}", e.getMessage(), e);
        }
    }

    private Users findUserOrNull(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElse(null);
    }
}