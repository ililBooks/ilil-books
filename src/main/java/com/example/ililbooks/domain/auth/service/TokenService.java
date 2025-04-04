package com.example.ililbooks.domain.auth.service;

import com.example.ililbooks.config.util.JwtUtil;
import com.example.ililbooks.domain.auth.entity.RefreshToken;
import com.example.ililbooks.domain.auth.repository.RefreshTokenRepository;
import com.example.ililbooks.domain.user.entity.User;
import com.example.ililbooks.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.ililbooks.global.exception.ErrorMessage.REFRESH_TOKEN_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    /* Access Token 생성 */
    public String createAccessToken(User user) {
        return jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getNickname(), user.getUserRole());
    }

    /* Refresh Token 생성 */
    public String createRefreshToken(User user) {
        RefreshToken refreshToken = refreshTokenRepository.save(new RefreshToken(user.getId()));
        return refreshToken.getToken();
    }

    public RefreshToken getRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException(REFRESH_TOKEN_NOT_FOUND.getMessage()));
    }
}