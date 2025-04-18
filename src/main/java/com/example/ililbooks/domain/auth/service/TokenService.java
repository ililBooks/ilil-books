package com.example.ililbooks.domain.auth.service;

import com.example.ililbooks.config.util.JwtUtil;
import com.example.ililbooks.domain.auth.entity.RefreshToken;
import com.example.ililbooks.domain.auth.repository.RefreshTokenRepository;
import com.example.ililbooks.domain.user.entity.Users;
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
    public String createAccessToken(Users users) {
        return jwtUtil.createAccessToken(users.getId(), users.getEmail(), users.getNickname(), users.getUserRole());
    }

    /* Refresh Token 생성 */
    public String createRefreshToken(Users users) {
        RefreshToken refreshToken = refreshTokenRepository.save(RefreshToken.of(users.getId()));
        return refreshToken.getToken();
    }

    /* Refresh Token 갱신 */
    public String updateRefreshToken(RefreshToken refreshToken) {
        refreshToken.updateToken();
        RefreshToken updatedToken = refreshTokenRepository.save(refreshToken);
        return updatedToken.getToken();
    }

    public RefreshToken findRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException(REFRESH_TOKEN_NOT_FOUND.getMessage()));
    }
}