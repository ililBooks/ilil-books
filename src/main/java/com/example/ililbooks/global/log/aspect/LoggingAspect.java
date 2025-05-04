package com.example.ililbooks.global.log.aspect;

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

    private final LogMaskingUtil logMaskingUtil;
    private final SystemLogService systemLogService;

    @Around("execution(* com.example.ililbooks.domain..controller..*Controller.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        try {
            Object requestBody = extractRequestBody(joinPoint.getArgs());
            Object maskedRequestBody = logMaskingUtil.maskIfNecessary(requestBody);

            LogRequest logRequest = LogRequest.of(traceId, method, uri, null, Instant.now(), maskedRequestBody, resolveActionType(uri, method), ip, userAgent, null, null);
            systemLogService.saveRequestLog(logRequest);

            Object result = joinPoint.proceed();

            Object maskedResponseBody = logMaskingUtil.maskIfNecessary(result);
            LogResponse logResponse = LogResponse.of(traceId, method, uri, null, Instant.now(), maskedResponseBody, resolveActionType(uri, method), ip, userAgent, "SUCCESS", null);
            systemLogService.saveResponseLog(logResponse);

            return result;
        } finally {
            MDC.remove("traceId");
        }
    }

    private Object extractRequestBody(Object[] args) {
        if (args == null) return null;
        for (Object arg : args) {
            if (!(arg instanceof HttpServletRequest)) {
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
}