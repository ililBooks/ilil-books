package com.example.ililbooks.domain.auth.service;

import com.example.ililbooks.config.util.JwtUtil;
import com.example.ililbooks.domain.auth.entity.RefreshToken;
import com.example.ililbooks.domain.auth.repository.RefreshTokenRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserService userService;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private TokenService tokenService;

    private Users users;

    @BeforeEach
    public void setUp() {
        users = Users.builder()
                .email("email@email.com")
                .nickname("nickname")
                .userRole(UserRole.ROLE_USER)
                .build();
    }

    /* createAccessToken */
    @Test
    void 토큰발급_AccessToken_발급_성공() {
        // given
        String accessToken = "accessToken";

        given(jwtUtil.createAccessToken(users.getId(), users.getEmail(), users.getNickname(), users.getUserRole())).willReturn(accessToken);

        // when
        String result = tokenService.createAccessToken(users);

        // then
        assertEquals(accessToken, result);
    }

    /* createRefreshToken */
    @Test
    void 토큰발급_RefreshToken_발급_성공() {
        // given
        RefreshToken mockRefreshToken = new RefreshToken(users.getId());

        given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(mockRefreshToken);

        // when
        String createdRefreshToken = tokenService.createRefreshToken(users);

        // then
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
        assertEquals(mockRefreshToken.getToken(), createdRefreshToken);
    }
}
