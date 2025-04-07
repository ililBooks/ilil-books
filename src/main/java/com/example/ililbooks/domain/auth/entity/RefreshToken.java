package com.example.ililbooks.domain.auth.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.UUID;

import static com.example.ililbooks.config.util.JwtUtil.REFRESH_TOKEN_TIME;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash(value = "refreshToken", timeToLive = REFRESH_TOKEN_TIME)
public class RefreshToken {

    @Id
    private String token;

    private Long userId;

    @Builder
    public RefreshToken(Long userId) {
        this.userId = userId;
        this.token = UUID.randomUUID().toString();
    }

    public String updateToken() {
        this.token = UUID.randomUUID().toString();
        return token;
    }
}
