package com.example.ililbooks.domain.auth.kakao.service;

import com.example.ililbooks.config.util.JwtUtil;
import com.example.ililbooks.global.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class TokenValidator {

    private final JwtUtil jwtUtil;

    public void validateRefreshToken(String token) {
        try {
            Claims claims = jwtUtil.extractClaims(token);

            if (claims.getExpiration().before(new Date())) {
                throw new UnauthorizedException("Refresh Token이 만료되었습니다.");
            }

        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException("Refresh Token이 만료되었습니다.");
        } catch (SecurityException e) {
            throw new UnauthorizedException("서명이 유효하지 않은 토큰입니다.");
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("유효하지 않은 토큰입니다.");
        }
    }
}