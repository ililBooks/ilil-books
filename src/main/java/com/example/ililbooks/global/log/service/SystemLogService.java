package com.example.ililbooks.global.log.service;

import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.repository.UserRepository;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.ErrorCode;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.global.log.collector.LogCollector;
import com.example.ililbooks.global.log.dto.request.LogRequest;
import com.example.ililbooks.global.log.dto.response.SystemLogResponse;
import com.example.ililbooks.global.log.entity.SystemLog;
import com.example.ililbooks.global.log.repository.SystemLogRepository;
import com.example.ililbooks.global.log.specification.SystemLogSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.ililbooks.global.exception.ErrorMessage.LOG_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemLogService {

    private final SystemLogRepository systemLogRepository;
    private final UserRepository userRepository;
    private final LogCollector logCollector;

    /*
     * 시스템 로그 저장 (정상 및 에러 로그 통합)
     */
    @Transactional
    public SystemLogResponse createSystemLog(AuthUser authUser, LogRequest logRequest) {
        Users user = findUserOrNull(authUser.getUserId());

        // 에러 코드 및 메시지 처리
        String errorMessage = null;
        String errorCodeStr = null;

        if (logRequest.errorCode() != null) {
            errorCodeStr = logRequest.errorCode();
            errorMessage = logRequest.errorCode();
        }

        String descriptionStr = (logRequest.description() != null) ? logRequest.description().toString() : "No description provided";

        // 로그 생성 - 정상 로그 또는 에러 로그
        SystemLog systemLog = createSystemLogEntity(logRequest.traceId(), user, logRequest, errorCodeStr, errorMessage);

        // 로그 저장
        SystemLog savedLog = systemLogRepository.save(systemLog);

        // 로그 응답 반환
        return SystemLogResponse.of(savedLog);
    }

    /*
     * 에러 로그 저장
     */
    @Transactional
    public void saveErrorLog(String traceId, Long userId, String actionType, String method, String uri, String errorMessage, String ip, String userAgent, ErrorCode errorCode) {
        Users user = findUserOrNull(userId);

        SystemLog systemLog = SystemLog.errorOf(traceId, user, actionType, method, uri, errorMessage, ip, userAgent, errorCode.name(), errorMessage);
        systemLogRepository.save(systemLog);
    }

    /*
     * traceId로 시스템 로그 조회
     */
    @Transactional(readOnly = true)
    public SystemLogResponse getSystemLogByTraceId(String traceId) {
        SystemLog systemLog = systemLogRepository.findByTraceId(traceId).orElseThrow(
                () -> new NotFoundException(LOG_NOT_FOUND.getMessage()));

        return SystemLogResponse.of(systemLog);
    }

    /*
     * 조건 검색 - traceId, actionType, ipAddress, 기간 필터
     */
    @Transactional(readOnly = true)
    public List<SystemLogResponse> searchSystemLogs(String traceId, String actionType, String ipAddress, String startDate, String endDate) {
        Specification<SystemLog> spec = SystemLogSpecification.withFilters(traceId, actionType, ipAddress);

        // 기간 필터 추가
        if (startDate != null || endDate != null) {
            Instant startInstant = startDate != null
                    ? LocalDate.parse(startDate).atStartOfDay().toInstant(ZoneOffset.UTC)
                    : Instant.EPOCH;

            Instant endInstant = endDate != null
                    ? LocalDate.parse(endDate).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
                    : Instant.now();

            spec = spec.and(SystemLogSpecification.withCreatedAtBetween(startInstant, endInstant));
        }

        // 로그 조회 - createdAt 기준 최신순 정렬
        List<SystemLog> logs = systemLogRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
        return logs.stream()
                .map(SystemLogResponse::of)
                .collect(Collectors.toList());
    }

    /*
     * 시스템 로그 삭제
     */
    @Transactional
    public void deleteSystemLogByTraceId(String traceId) {
        SystemLog systemLog = systemLogRepository.findByTraceId(traceId).orElseThrow(
                () -> new NotFoundException(LOG_NOT_FOUND.getMessage()));

        systemLogRepository.delete(systemLog);
    }

    private Users findUserOrNull(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElse(null);
    }

    /*
     * 요청 로그를 로깅 시스템에 전달
     */
    public void saveRequestLog(LogRequest logRequest) {
        log.debug("[SystemLogService] saveRequestLog 호출됨: {}", logRequest);
        logCollector.collectRequestLog(logRequest);  // 로그 컬렉터로 전달
    }

    /*
     * 로그 엔티티 생성
     */
    private SystemLog createSystemLogEntity(String traceId, Users user, LogRequest logRequest, String errorCodeStr, String errorMessage) {
        if (errorCodeStr == null) {
            return SystemLog.of(traceId, user, logRequest.actionType(), logRequest.method(), logRequest.url(), logRequest.description().toString(), logRequest.ip(), logRequest.userAgent());
        }
        return SystemLog.errorOf(traceId, user, logRequest.actionType(), logRequest.method(), logRequest.url(), logRequest.description().toString(), logRequest.ip(), logRequest.userAgent(), errorCodeStr, errorMessage);
    }
}
