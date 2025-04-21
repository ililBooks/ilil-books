package com.example.ililbooks.domain.auth.service;

import com.example.ililbooks.client.naver.NaverClient;
import com.example.ililbooks.client.naver.dto.NaverApiProfileResponse;
import com.example.ililbooks.client.naver.dto.NaverApiResponse;
import com.example.ililbooks.domain.auth.naver.dto.request.AuthNaverAccessTokenRequest;
import com.example.ililbooks.domain.auth.dto.response.AuthTokensResponse;
import com.example.ililbooks.domain.auth.naver.service.AuthNaverService;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.LoginType;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.domain.user.service.UserSocialService;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.UUID;

import static com.example.ililbooks.domain.user.enums.LoginType.NAVER;
import static com.example.ililbooks.global.exception.ErrorMessage.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuthNaverServiceTest {

    @Mock
    private NaverClient naverClient;

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @Mock
    private UserSocialService userSocialService;

    @InjectMocks
    private AuthNaverService authNaverService;

    public static final String TEST_ISSUED_ACCESS_TOKEN = "AAAAN07x9hiyxl7233VJ6qIxnuxY1PVv_OM2YOpK_wQyzjzBXsNvIrW4p1XenFfttv-ALpb_Hd4CBYzM4hq1WEnLSQs";

    //request
    public static final AuthNaverAccessTokenRequest AUTH_NAVER_ACCESS_TOKEN_REQUEST = new AuthNaverAccessTokenRequest(TEST_ISSUED_ACCESS_TOKEN);

    //users
    public static final Users TEST_NAVER_USERS = Users.of(
            "example@mail.com",
            "닉네임",
            NAVER
    );

    //response
    public static final NaverApiProfileResponse NAVER_API_PROFILE_RESPONSE = new NaverApiProfileResponse("닉네임", "example@naver.com", "010-1234-5678");
    public static final NaverApiResponse NAVER_API_RESPONSE = new NaverApiResponse("accessToken", "refreshToken", "Bearer", 3600);
    public static final AuthTokensResponse AUTH_TOKENS_RESPONSE = AuthTokensResponse.builder()
            .accessToken("access-token")
            .refreshToken("refresh-token")
            .build();

    @Test
    void 네이버_로그인_인증_요청_성공() {
        //given
        URI expectedUri = URI.create("https://nid.naver.com/oauth2.0/authorize");
        given(naverClient.getRedirectUrl()).willReturn(expectedUri);

        //when
        URI result = authNaverService.getNaverLoginRedirectUrl();

        //then
        assertEquals(expectedUri, result);
        verify(naverClient).getRedirectUrl();
    }

    @Test
    void 네이버_소셜_로그인_접근_토큰_발급_성공 () {
        //given
        String code = "issued-code";
        String state = String.valueOf(UUID.randomUUID());

        given(naverClient.issueToken(code, state)).willReturn(NAVER_API_RESPONSE);

        //when
        NaverApiResponse result = authNaverService.requestToken(code, state);

        //then
        assertEquals(NAVER_API_RESPONSE, result);
        verify(naverClient).issueToken(code, state);
    }

    @Test
    void 이미_존재하는_이메일_있어_네이버_회원가입_실패() {
        //given
        givenNaverProfile();
        given(userService.existsByEmailAndLoginType(anyString(), any(LoginType.class))).willReturn(true);

        //when & then
        assertThrows(BadRequestException.class,
                () -> authNaverService.signUp(AUTH_NAVER_ACCESS_TOKEN_REQUEST),
                DUPLICATE_EMAIL.getMessage());
    }

    @Test
    void 네이버_회원가입_성공() {
        //given
        givenNaverProfile();
        given(userService.existsByEmailAndLoginType(anyString(), any(LoginType.class))).willReturn(false);
        given(userSocialService.saveUser(any(Users.class))).willReturn(TEST_NAVER_USERS);
        given(authService.getTokenResponse(any(Users.class))).willReturn(AUTH_TOKENS_RESPONSE);

        //when
        AuthTokensResponse result = authNaverService.signUp(AUTH_NAVER_ACCESS_TOKEN_REQUEST);

        //then
        assertEquals(AUTH_TOKENS_RESPONSE.accessToken(), result.accessToken());
        assertEquals(AUTH_TOKENS_RESPONSE.refreshToken(), result.refreshToken());
    }

    @Test
    void 조회한_프로필_이메일로_찾아지는_유저가_존재하지_않아_네이버_로그인_실패() {
        //given
        givenNaverProfile();
        given(userService.findByEmailAndLoginTypeOrElseThrow(anyString(), any(LoginType.class))).willThrow(new UnauthorizedException());

        //when & then
        assertThrows(UnauthorizedException.class,
                () -> authNaverService.signIn(AUTH_NAVER_ACCESS_TOKEN_REQUEST),
                USER_EMAIL_NOT_FOUND.getMessage()
        );
    }

    @Test
    void 탈퇴한_유저로_네이버_로그인_실패() {
        //given
        ReflectionTestUtils.setField(TEST_NAVER_USERS, "isDeleted", true);

        givenNaverProfile();
        given(userService.findByEmailAndLoginTypeOrElseThrow(anyString(), any(LoginType.class))).willReturn(TEST_NAVER_USERS);

        //when & then
        assertThrows(UnauthorizedException.class,
                () -> authNaverService.signIn(AUTH_NAVER_ACCESS_TOKEN_REQUEST),
                DEACTIVATED_USER_EMAIL.getMessage()
        );
    }

    @Test
    void 네이버_로그인_성공() {
        //given
        ReflectionTestUtils.setField(TEST_NAVER_USERS, "isDeleted", false);
        ReflectionTestUtils.setField(TEST_NAVER_USERS, "loginType", LoginType.NAVER);

        givenNaverProfile();
        given(userService.findByEmailAndLoginTypeOrElseThrow(anyString(), any(LoginType.class))).willReturn(TEST_NAVER_USERS);
        given(authService.getTokenResponse(TEST_NAVER_USERS)).willReturn(AUTH_TOKENS_RESPONSE);

        //when
        AuthTokensResponse result = authNaverService.signIn(AUTH_NAVER_ACCESS_TOKEN_REQUEST);

        //then
        assertEquals(AUTH_TOKENS_RESPONSE.accessToken(), result.accessToken());
        assertEquals(AUTH_TOKENS_RESPONSE.refreshToken(), result.refreshToken());
    }

    private void givenNaverProfile() {
        given(naverClient.findProfile(TEST_ISSUED_ACCESS_TOKEN)).willReturn(NAVER_API_PROFILE_RESPONSE);
    }
}