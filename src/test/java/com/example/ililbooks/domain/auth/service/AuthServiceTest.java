package com.example.ililbooks.domain.auth.service;

import com.example.ililbooks.domain.auth.dto.request.AuthSigninRequest;
import com.example.ililbooks.domain.auth.dto.request.AuthSignupRequest;
import com.example.ililbooks.domain.auth.dto.response.AuthTokensResponse;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static com.example.ililbooks.global.exception.ErrorMessage.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private TokenService tokenService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private AuthSignupRequest successSignup;
    private AuthSignupRequest passwordCheckErrorSignup;
    private AuthSigninRequest successSignin;
    private Users users;

    @BeforeEach
    public void setUp() {
        passwordCheckErrorSignup = AuthSignupRequest.builder()
                .email("email@email.com")
                .nickname("nickname")
                .password("password1234")
                .passwordCheck("password1234!")
                .userRole("USER_ROLE")
                .build();

        successSignup = AuthSignupRequest.builder()
                .email("email@email.com")
                .nickname("nickname")
                .password("password1234")
                .passwordCheck("password1234")
                .userRole("USER_ROLE")
                .build();

        successSignin = AuthSigninRequest.builder()
                .email("email@email.com")
                .password("password1234")
                .build();

        users = Users.builder()
                .email(successSignup.getEmail())
                .nickname(successSignup.getNickname())
                .userRole(UserRole.ROLE_USER)
                .build();

    }

    @Test
    void 회원가입_비밀번호_확인_불일치_실패() {
        // given

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> authService.signup(passwordCheckErrorSignup));
        assertEquals(badRequestException.getMessage(), PASSWORD_CONFIRMATION_MISMATCH.getMessage());
    }

    @Test
    void 회원가입_성공() {
        // given
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

//        given(userService.saveUser(any(String.class), any(String.class), any(String.class), any(String.class))).willReturn(users);
        given(tokenService.createAccessToken(any(Users.class))).willReturn(accessToken);
        given(tokenService.createRefreshToken(any(Users.class))).willReturn(refreshToken);

        // when
        AuthTokensResponse result = authService.signup(successSignup);

        // then
        assertEquals(accessToken, result.getAccessToken());
        assertEquals(refreshToken, result.getRefreshToken());
    }

    @Test
    void 로그인_삭제된_유저의_이메일일_경우_실패() {
        // given
        ReflectionTestUtils.setField(users, "deletedAt", LocalDateTime.now());

        given(userService.findByEmailOrElseThrow(any(String.class))).willReturn(users);

        // when & then
        UnauthorizedException unauthorizedException = assertThrows(UnauthorizedException.class,
                () -> authService.signin(successSignin));
        assertEquals(unauthorizedException.getMessage(), DEACTIVATED_USER_EMAIL.getMessage());
    }

    @Test
    void 로그인_비밀번호가_일치하지_않을_경우_실패() {
        // given
        ReflectionTestUtils.setField(users, "deletedAt", null);

        given(userService.findByEmailOrElseThrow(any(String.class))).willReturn(users);
        given(passwordEncoder.matches(successSignin.getPassword(), users.getPassword())).willReturn(false);

        // when & then
        UnauthorizedException unauthorizedException = assertThrows(UnauthorizedException.class,
                () -> authService.signin(successSignin));
        assertEquals(unauthorizedException.getMessage(), INVALID_PASSWORD.getMessage());
    }

    @Test
    void 로그인_성공() {
        // given
        ReflectionTestUtils.setField(users, "deletedAt", null);

        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        given(userService.findByEmailOrElseThrow(any(String.class))).willReturn(users);
        given(passwordEncoder.matches(successSignin.getPassword(), users.getPassword())).willReturn(true);
        given(tokenService.createAccessToken(any(Users.class))).willReturn(accessToken);
        given(tokenService.createRefreshToken(any(Users.class))).willReturn(refreshToken);

        // when
        AuthTokensResponse result = authService.signin(successSignin);

        // then
        assertEquals(accessToken, result.getAccessToken());
        assertEquals(refreshToken, result.getRefreshToken());
    }

//    @Test
//    void 토큰_재발급_성공() {
//        // given
//        String refreshToken = "refreshToken";
//
//        String reissuedAccessToken = "reissued-accessToken";
//        String reissuedRefreshToken = "reissued-refreshToken";
//
//        given(tokenService.reissueToken(refreshToken)).willReturn(user);
//        given(tokenService.createAccessToken(any(User.class))).willReturn(reissuedAccessToken);
//        given(tokenService.createRefreshToken(any(User.class))).willReturn(reissuedRefreshToken);
//
//        // when
//        AuthTokensResponse result = authService.reissueAccessToken(refreshToken);
//
//        // then
//        assertEquals(reissuedAccessToken, result.getAccessToken());
//        assertEquals(reissuedRefreshToken, result.getRefreshToken());
//    }
}
