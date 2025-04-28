package com.example.ililbooks.global.log.entity;

import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.global.entity.TimeStamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "system_logs")
public class SystemLog extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trace_id")
    private String traceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users users;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "method")
    private String method;

    @Column(name = "url")
    private String url;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Builder
    private SystemLog(String traceId, Users users, String actionType, String method, String url, String description, String ipAddress, String userAgent, String errorCode, String errorMessage) {
        this.traceId = traceId;
        this.users = users;
        this.actionType = actionType;
        this.method = method;
        this.url = url;
        this.description = description;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /*
     * 일반 SystemLog 생성
     */
    public static SystemLog of(String traceId, Users users, String actionType, String method, String url, String description, String ipAddress, String userAgent) {
        return new SystemLog(traceId, users, actionType, method, url, description, ipAddress, userAgent, null, null);
    }

    /*
     * 오류 SystemLog 생성
     */
    public static SystemLog errorOf(String traceId, Users users, String actionType, String method, String url, String description, String ipAddress, String userAgent, String errorCode, String errorMessage) {
        return new SystemLog(traceId, users, actionType, method, url, description, ipAddress, userAgent, errorCode, errorMessage);
    }
}
