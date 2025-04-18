package com.example.ililbooks.domain.auth.service;

import com.example.ililbooks.client.google.GoogleClient;
import com.example.ililbooks.client.google.dto.GoogleApiProfileResponse;
import com.example.ililbooks.client.google.dto.GoogleApiResponse;
import com.example.ililbooks.domain.auth.dto.request.AuthGoogleAccessTokenRequest;
import com.example.ililbooks.domain.auth.dto.response.AuthTokensResponse;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.LoginType;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.domain.user.service.UserSocialService;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;

import static com.example.ililbooks.global.exception.ErrorMessage.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthGoogleServiceTest {

    @Mock
    private GoogleClient googleClient;
    @Mock
    private UserService userService;
    @Mock
    private AuthService authService;
    @Mock
    private UserSocialService userSocialService;

    @InjectMocks
    private AuthGoogleService authGoogleService;

    private GoogleApiResponse buildedGoogleApiResponse;
    private AuthGoogleAccessTokenRequest authGoogleAccessTokenRequest;
    private GoogleApiProfileResponse googleApiProfileResponse;
    private AuthTokensResponse authTokensResponse;
    private Users users;

    @BeforeEach
    public void setUp() {
        buildedGoogleApiResponse = GoogleApiResponse.builder()
                .accessToken("access-token")
                .build();

        authGoogleAccessTokenRequest = AuthGoogleAccessTokenRequest.builder()
                .accessToken("access-token")
                .build();

        googleApiProfileResponse = GoogleApiProfileResponse.builder()
                .email("email@email.com")
                .name("nickname")
                .build();

        authTokensResponse = AuthTokensResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        users = Users.builder()
                .id(1L)
                .email("email@email.com")
                .nickname("nickname")
                .userRole(UserRole.ROLE_USER)
                .build();
    }

    /* createAuthorizationUrl */
    @Test
    void 구글_로그인_인증_요청_성공() {
        // given
        URI expectedUri = URI.create("https://accounts.google.com/o/oauth2/auth");
        given(googleClient.createAuthorizationUrl()).willReturn(expectedUri);

        // when
        URI result = authGoogleService.createAuthorizationUrl();

        // then
        assertEquals(expectedUri, result);
        verify(googleClient).createAuthorizationUrl();
    }

    /* requestToken */
    @Test
    void 구글_소셜로그인_접근_토큰_발급_성공() {
        // given
        String code = "issued-code";
        GoogleApiResponse expectGoogleApiResponse = GoogleApiResponse.builder()
                .accessToken("access-token")
                .build();

        given(googleClient.issueToken(anyString())).willReturn(buildedGoogleApiResponse);

        // when
        GoogleApiResponse result = authGoogleService.requestToken(code);

        // then
        assertEquals(expectGoogleApiResponse.accessToken(), result.accessToken());
        verify(googleClient).issueToken(anyString());
    }

    /* signUp */
    @Test
    void 구글_소셜로그인_회원가입_이미_존재하는_이메일의_가입_실패() {
        // given
        given(googleClient.findProfile(anyString())).willReturn(googleApiProfileResponse);
        given(userService.existsByEmail(anyString())).willReturn(true);

        // when & given
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> authGoogleService.signUp(authGoogleAccessTokenRequest));
        assertEquals(badRequestException.getMessage(), DUPLICATE_EMAIL.getMessage());
    }

    @Test
    void 구글_소셜로그인_회원가입_성공() {
        // given
        given(googleClient.findProfile(anyString())).willReturn(googleApiProfileResponse);
        given(userService.existsByEmail(anyString())).willReturn(false);
        given(userSocialService.saveUser(any(Users.class))).willReturn(users);
        given(authService.getTokenResponse(any(Users.class))).willReturn(authTokensResponse);

        // when
        AuthTokensResponse result = authGoogleService.signUp(authGoogleAccessTokenRequest);

        // when & given
        assertEquals(authTokensResponse.accessToken(), result.accessToken());
        assertEquals(authTokensResponse.refreshToken(), result.refreshToken());
    }

    /* signIn */
    @Test
    void 구글_소셜로그인_로그인_삭제된_유저의_로그인_실패() {
        // given
        ReflectionTestUtils.setField(users, "isDeleted", true);

        given(googleClient.findProfile(anyString())).willReturn(googleApiProfileResponse);
        given(userService.findByEmailOrElseThrow(anyString())).willReturn(users);

        // when & given
        UnauthorizedException unauthorizedException = assertThrows(UnauthorizedException.class,
                () -> authGoogleService.signIn(authGoogleAccessTokenRequest));
        assertEquals(unauthorizedException.getMessage(), DEACTIVATED_USER_EMAIL.getMessage());
    }

    @Test
    void 구글_소셜로그인_로그인_LoginType이_GOOGLE이_아니라면_실패() {
        // given
        ReflectionTestUtils.setField(users, "isDeleted", false);
        ReflectionTestUtils.setField(users, "loginType", LoginType.EMAIL);

        given(googleClient.findProfile(anyString())).willReturn(googleApiProfileResponse);
        given(userService.findByEmailOrElseThrow(anyString())).willReturn(users);

        // when & given
        UnauthorizedException unauthorizedException = assertThrows(UnauthorizedException.class,
                () -> authGoogleService.signIn(authGoogleAccessTokenRequest));
        assertEquals(unauthorizedException.getMessage(), NOT_GOOGLE_USER.getMessage());
    }

    @Test
    void 구글_소셜로그인_로그인_성공() {
        // given
        ReflectionTestUtils.setField(users, "isDeleted", false);
        ReflectionTestUtils.setField(users, "loginType", LoginType.GOOGLE);

        given(googleClient.findProfile(anyString())).willReturn(googleApiProfileResponse);
        given(userService.findByEmailOrElseThrow(anyString())).willReturn(users);
        given(authService.getTokenResponse(any(Users.class))).willReturn(authTokensResponse);

        // when
        AuthTokensResponse result = authGoogleService.signIn(authGoogleAccessTokenRequest);

        // then
        assertEquals(authTokensResponse.accessToken(), result.accessToken());
        assertEquals(authTokensResponse.refreshToken(), result.refreshToken());
    }
}