package com.example.ililbooks.domain.auth.service;

import com.example.ililbooks.config.util.JwtUtil;
import com.example.ililbooks.domain.auth.entity.RefreshToken;
import com.example.ililbooks.domain.auth.repository.RefreshTokenRepository;
import com.example.ililbooks.domain.user.entity.User;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.global.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.example.ililbooks.domain.auth.enums.TokenState.INVALIDATED;
import static com.example.ililbooks.global.exception.ErrorMessage.EXPIRED_REFRESH_TOKEN;
import static com.example.ililbooks.global.exception.ErrorMessage.REFRESH_TOKEN_NOT_FOUND;
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

    private User user;

    @BeforeEach
    public void setUp() {
        user = User.builder()
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

        given(jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getNickname(), user.getUserRole())).willReturn(accessToken);

        // when
        String result = tokenService.createAccessToken(user);

        // then
        assertEquals(accessToken, result);
    }

    /* createRefreshToken */
    @Test
    void 토큰발급_RefreshToken_발급_성공() {
        // given
        RefreshToken mockRefreshToken = new RefreshToken(user.getId());

        given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(mockRefreshToken);

        // when
        String createdRefreshToken = tokenService.createRefreshToken(user);

        // then
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
        assertEquals(mockRefreshToken.getToken(), createdRefreshToken);
    }

    /* reissueToken */
    @Test
    void 토큰유효성검사_비활성_상태일때_실패() {
        // given
        String refreshToken = "refresh-token";

        RefreshToken mockRefreshToken = mock(RefreshToken.class);

        given(refreshTokenRepository.findByToken(any(String.class))).willReturn(Optional.of(mockRefreshToken));
        given(mockRefreshToken.getTokenState()).willReturn(INVALIDATED);

        // when & then
        UnauthorizedException unauthorizedException = assertThrows(UnauthorizedException.class,
                () -> tokenService.reissueToken(refreshToken));
        assertEquals(unauthorizedException.getMessage(), EXPIRED_REFRESH_TOKEN.getMessage());
    }

    @Test
    void 토큰검색_토큰이_없을_시_실패() {
        //given
        String refreshToken = "refresh-token";

        given(refreshTokenRepository.findByToken(any(String.class))).willReturn(Optional.empty());

        // when & then
        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> tokenService.reissueToken(refreshToken));
        assertEquals(notFoundException.getMessage(), REFRESH_TOKEN_NOT_FOUND.getMessage());
    }

    @Test
    void 토큰유효성검사_성공() {
        // given
        String refreshToken = "refresh-token";

        RefreshToken mockRefreshToken = mock(RefreshToken.class);

        given(refreshTokenRepository.findByToken(any(String.class))).willReturn(Optional.of(mockRefreshToken));
        given(userService.getUserById(anyLong())).willReturn(user);

        // when
        User result = tokenService.reissueToken(refreshToken);

        // then
        assertNotNull(result);
        verify(mockRefreshToken, times(1)).updateTokenStatus(INVALIDATED);
    }
}
