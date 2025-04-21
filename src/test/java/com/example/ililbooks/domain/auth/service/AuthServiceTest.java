package com.example.ililbooks.domain.auth.service;

import com.example.ililbooks.domain.auth.dto.request.AuthSignInRequest;
import com.example.ililbooks.domain.auth.dto.request.AuthSignUpRequest;
import com.example.ililbooks.domain.auth.dto.response.AuthTokensResponse;
import com.example.ililbooks.domain.auth.entity.RefreshToken;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.LoginType;
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

import static com.example.ililbooks.global.exception.ErrorMessage.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

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

    private AuthSignUpRequest successSignUp;
    private AuthSignUpRequest passwordCheckErrorSignUp;
    private AuthSignInRequest successSignIn;
    private Users users;

    @BeforeEach
    public void setUp() {
        passwordCheckErrorSignUp = AuthSignUpRequest.builder()
                .email("email@email.com")
                .nickname("nickname")
                .password("password1234")
                .passwordCheck("password1234!")
                .userRole("USER_ROLE")
                .build();

        successSignUp = AuthSignUpRequest.builder()
                .email("email@email.com")
                .nickname("nickname")
                .password("password1234")
                .passwordCheck("password1234")
                .userRole("USER_ROLE")
                .build();

        successSignIn = AuthSignInRequest.builder()
                .email("email@email.com")
                .password("password1234")
                .build();

        users = Users.builder()
                .email(successSignUp.email())
                .nickname(successSignUp.nickname())
                .userRole(UserRole.ROLE_USER)
                .build();
    }

    @Test
    void 회원가입_비밀번호_확인_불일치_실패() {
        // given

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> authService.signUp(passwordCheckErrorSignUp));
        assertEquals(badRequestException.getMessage(), PASSWORD_CONFIRMATION_MISMATCH.getMessage());
    }

    @Test
    void 회원가입_성공() {
        // given
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        given(userService.saveUser(any(AuthSignUpRequest.class))).willReturn(users);
        given(tokenService.createAccessToken(any(Users.class))).willReturn(accessToken);
        given(tokenService.createRefreshToken(any(Users.class))).willReturn(refreshToken);

        // when
        AuthTokensResponse result = authService.signUp(successSignUp);

        // then
        assertEquals(accessToken, result.accessToken());
        assertEquals(refreshToken, result.refreshToken());
    }

    @Test
    void 로그인_삭제된_유저의_이메일일_경우_실패() {
        // given
        ReflectionTestUtils.setField(users, "isDeleted", true);

        given(userService.findByEmailAndLoginTypeOrElseThrow(anyString(), any(LoginType.class))).willReturn(users);

        // when & then
        UnauthorizedException unauthorizedException = assertThrows(UnauthorizedException.class,
                () -> authService.signIn(successSignIn));
        assertEquals(unauthorizedException.getMessage(), DEACTIVATED_USER_EMAIL.getMessage());
    }

    @Test
    void 로그인_비밀번호가_일치하지_않을_경우_실패() {
        // given
        ReflectionTestUtils.setField(users, "isDeleted", false);

        given(userService.findByEmailAndLoginTypeOrElseThrow(anyString(), any(LoginType.class))).willReturn(users);
        given(passwordEncoder.matches(successSignIn.password(), users.getPassword())).willReturn(false);

        // when & then
        UnauthorizedException unauthorizedException = assertThrows(UnauthorizedException.class,
                () -> authService.signIn(successSignIn));
        assertEquals(unauthorizedException.getMessage(), INVALID_PASSWORD.getMessage());
    }

    @Test
    void 로그인_성공() {
        // given
        ReflectionTestUtils.setField(users, "isDeleted", false);

        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        given(userService.findByEmailAndLoginTypeOrElseThrow(anyString(), any(LoginType.class))).willReturn(users);
        given(passwordEncoder.matches(successSignIn.password(), users.getPassword())).willReturn(true);
        given(tokenService.createAccessToken(any(Users.class))).willReturn(accessToken);
        given(tokenService.createRefreshToken(any(Users.class))).willReturn(refreshToken);

        // when
        AuthTokensResponse result = authService.signIn(successSignIn);

        // then
        assertEquals(accessToken, result.accessToken());
        assertEquals(refreshToken, result.refreshToken());
    }

    @Test
    void 토큰_재발급_성공() {
        // given
        ReflectionTestUtils.setField(users, "id", 1L);

        String requestRefreshToken = "refresh-token";

        String reissuedAccessToken = "reissued-accessToken";
        String reissuedRefreshToken = "reissued-refreshToken";

        RefreshToken mokeRefreshToken = spy(RefreshToken.builder().userId(users.getId()).build());

        given(tokenService.findRefreshToken(any(String.class))).willReturn(mokeRefreshToken);
        given(userService.findByIdOrElseThrow(anyLong())).willReturn(users);
        given(tokenService.createAccessToken(any(Users.class))).willReturn(reissuedAccessToken);
        given(tokenService.updateRefreshToken(any(RefreshToken.class))).willReturn(reissuedRefreshToken);

        // when
        AuthTokensResponse result = authService.reissueToken(requestRefreshToken);

        // then
        assertEquals(reissuedAccessToken, result.accessToken());
        assertEquals(reissuedRefreshToken, result.refreshToken());
    }
}
