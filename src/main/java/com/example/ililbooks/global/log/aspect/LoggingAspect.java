package com.example.ililbooks.global.log.aspect;

import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.ErrorCode;
import com.example.ililbooks.global.exception.HandledException;
import com.example.ililbooks.global.log.collector.LogCollector;
import com.example.ililbooks.global.log.dto.request.LogRequest;
import com.example.ililbooks.global.log.dto.response.LogResponse;
import com.example.ililbooks.global.log.service.SystemLogService;
import com.example.ililbooks.global.log.util.LogMaskingUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final LogCollector logCollector;
    private final LogMaskingUtil logMaskingUtil;
    private final SystemLogService systemLogService;

    @Around("execution(* com.example.ililbooks.domain..controller..*Controller.*(..))")
    public Object logRequestAndResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        Long userId = resolveUserId(joinPoint.getArgs());
        String actionType = resolveActionType(uri, method);

        try {
            Instant requestTime = Instant.now();
            Object requestBody = extractRequestBody(joinPoint.getArgs());
            Object maskedRequestBody = logMaskingUtil.maskIfNecessary(requestBody);

            LogRequest logRequest = new LogRequest(traceId, method, uri, userId, requestTime, maskedRequestBody, actionType, ip, userAgent, null, null);
            logCollector.collectRequestLog(logRequest);

            // 실제 메서드 실행
            Object result = joinPoint.proceed();
            Object maskedResponseBody = logMaskingUtil.maskIfNecessary(result);

            LogResponse logResponse = new LogResponse(traceId, method, uri, userId, Instant.now(), maskedResponseBody, actionType, ip, userAgent, null, null);
            logCollector.collectResponseLog(logResponse);

            // 정상 요청 저장
            AuthUser authUser = new AuthUser(userId, "", "", null);
            systemLogService.createSystemLog(authUser, logRequest);

            return result;

        } catch (Throwable e) {
            log.warn("[API ERROR] traceId={}, uri={}, error={}", traceId, uri, e.getMessage());

            // 예외 처리
            ErrorCode errorCode;
            if (e instanceof HandledException handledException) {
                errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
                log.error("HandledException occurred: {}, message: {}", handledException.getClass().getName(), handledException.getMessage());
            } else {
                errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
            }

            // 에러 로그 저장
            systemLogService.saveErrorLog(traceId, userId, actionType, method, uri, e.getMessage(), ip, userAgent, errorCode);

            throw e;

        } finally {
            MDC.remove("traceId");
        }
    }

    private Long resolveUserId(Object[] args) {
        if (args == null) return null;
        for (Object arg : args) {
            if (arg instanceof AuthUser authUser) {
                return authUser.getUserId();
            }
        }
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof AuthUser authUser) {
                return authUser.getUserId();
            }
        } catch (Exception e) {
            log.debug("SecurityContextHolder 에서 AuthUser 추출 실패: {}", e.getMessage());
        }
        return null;
    }

    private Object extractRequestBody(Object[] args) {
        if (args == null) return null;
        for (Object arg : args) {
            if (arg == null) continue;
            if (!(arg instanceof HttpServletRequest) && !(arg instanceof AuthUser)) {
                return arg;
            }
        }
        return null;
    }

    private String resolveActionType(String uri, String method) {
        if (uri.contains("/login")) return "LOGIN";
        if (uri.contains("/signup")) return "SIGNUP";
        if (uri.contains("/order")) return "ORDER";
        if (uri.contains("/reservation")) return "RESERVATION";
        return switch (method.toUpperCase()) {
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> "READ";
        };
    }

    /**
     * 예외 메시지로 ErrorCode 를 찾는다
     */
    private ErrorCode findErrorCodeByMessage(String message) {
        for (ErrorCode code : ErrorCode.values()) {
            if (code.getErrorMessage().getMessage().equals(message)) {
                return code;
            }
        }
        return ErrorCode.INTERNAL_SERVER_ERROR;
    }
}