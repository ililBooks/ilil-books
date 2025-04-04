package com.example.ililbooks.domain.auth.service;

import com.example.ililbooks.config.util.JwtUtil;
import com.example.ililbooks.domain.auth.entity.RefreshToken;
import com.example.ililbooks.domain.auth.enums.TokenState;
import com.example.ililbooks.domain.auth.repository.RefreshTokenRepository;
import com.example.ililbooks.domain.user.entity.User;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.ililbooks.domain.auth.enums.TokenState.INVALIDATED;
import static com.example.ililbooks.global.exception.ErrorMessage.EXPIRED_REFRESH_TOKEN;
import static com.example.ililbooks.global.exception.ErrorMessage.REFRESH_TOKEN_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;
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

    /* Refresh Token 유효성 검사 */
    public User reissueToken(String token) {

        RefreshToken refreshToken = getByToken(token);

        if (refreshToken.getTokenState() == INVALIDATED) {
            throw new UnauthorizedException(EXPIRED_REFRESH_TOKEN.getMessage());
        }

        refreshToken.updateTokenStatus(INVALIDATED);
        refreshTokenRepository.save(refreshToken);

        return userService.getUserById(refreshToken.getUserId());
    }

    private RefreshToken getByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException(REFRESH_TOKEN_NOT_FOUND.getMessage()));
    }
}