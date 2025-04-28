package com.example.ililbooks.global.log.controller;

import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import com.example.ililbooks.global.log.dto.request.LogRequest;
import com.example.ililbooks.global.log.dto.response.SystemLogResponse;
import com.example.ililbooks.global.log.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.ADMIN;

@RestController
@RequestMapping("/api/v1/system-logs")
@RequiredArgsConstructor
public class SystemLogController {

    private final SystemLogService systemLogService;

    /*
     * 시스템 로그 등록 (관리자만 가능)
     */
    @Secured(ADMIN)
    @PostMapping
    public Response<SystemLogResponse> createSystemLog(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody LogRequest logRequest
    ) {
        // 서비스 메서드 호출 후, 반환된 값에 대해 Response.of() 사용
        return Response.of(systemLogService.createSystemLog(authUser, logRequest));
    }

    /*
     * traceId로 로그 목록 조회
     */
    @Secured(ADMIN)
    @GetMapping("/{traceId}")
    public Response<SystemLogResponse> getSystemLogs(
            @PathVariable String traceId
    ) {
        return Response.of(systemLogService.getSystemLogByTraceId(traceId));
    }

    /*
     * 복합 조건 검색
     */
    @Secured(ADMIN)
    @GetMapping("/search")
    public Response<List<SystemLogResponse>> searchSystemLogs(
            @RequestParam(required = false) String traceId,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        return Response.of(systemLogService.searchSystemLogs(traceId, actionType, ipAddress, startDate, endDate));
    }

    /*
     * 시스템 로그 삭제 (관리자만 가능)
     */
    @Secured(ADMIN)
    @DeleteMapping("/{traceId}")
    public Response<Void> deleteSystemLog(
            @PathVariable String traceId
    ) {
        // 서비스 메서드 호출 후, Response.empty() 반환
        systemLogService.deleteSystemLogByTraceId(traceId);
        return Response.empty();
    }

    /*
     * CloudWatch 로깅 테스트용
     */
    @PostMapping("/test-cloudwatch")
    public ResponseEntity<String> testCloudWatchLogging() {
        systemLogService.saveRequestLog(new LogRequest("TEST_TRACE_ID", "POST", "/api/v1/log/test-cloudwatch", 1L, Instant.now(), "테스트 요청 바디", "SAVE_LOG_ACTION", "127.0.0.1", "JUnit/Mock", null, null));
        return ResponseEntity.ok("CloudWatch Logging Test Completed");
    }
}
