package com.example.ililbooks.domain.auth.service;

import com.example.ililbooks.config.util.JwtUtil;
import com.example.ililbooks.domain.auth.entity.RefreshToken;
import com.example.ililbooks.domain.auth.repository.RefreshTokenRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.global.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.example.ililbooks.global.exception.ErrorMessage.REFRESH_TOKEN_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenService tokenService;

    private Users users;
    private RefreshToken savedToken;
    private RefreshToken updatedToken;

    @BeforeEach
    public void setUp() {
        users = Users.builder()
                .id(1L)
                .email("email@email.com")
                .nickname("nickname")
                .userRole(UserRole.ROLE_USER)
                .build();

        savedToken = RefreshToken.builder()
                .userId(1L)
                .token("refresh-token")
                .build();

        updatedToken = RefreshToken.builder()
                .userId(1L)
                .token("updated-token")
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
        String expectedToken = "refresh-token";

        given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(savedToken);

        // when
        String result = tokenService.createRefreshToken(users);

        // then
        assertEquals(expectedToken, result);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    /* updateRefreshToken */
    @Test
    void 리플래시_토큰_갱신_성공() {
        // given
        given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(updatedToken);

        // when
        String result = tokenService.updateRefreshToken(savedToken);

        // then
        assertEquals(updatedToken.getToken(), result);
    }

    /* findRefreshToken */
    @Test
    void 리플래시_토큰_찾기_토큰이_없어_실패() {
        //given
        String token = "refresh-token";

        given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.empty());

        // when & given
        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> tokenService.findRefreshToken(token));
        assertEquals(notFoundException.getMessage(), REFRESH_TOKEN_NOT_FOUND.getMessage());
    }

    @Test
    void 리플래시_토큰_찾기_성공() {
        //given
        String token = "refresh-token";

        given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.of(savedToken));

        // when
        RefreshToken refreshToken = tokenService.findRefreshToken(token);

        // then
        assertEquals(token, refreshToken.getToken());
    }
}
