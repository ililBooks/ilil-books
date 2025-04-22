package com.example.ililbooks.domain.auth.kakao.service;

import com.example.ililbooks.client.kakao.KakaoClient;
import com.example.ililbooks.client.kakao.dto.AuthKakaoResponse;
import com.example.ililbooks.client.kakao.dto.AuthKakaoResponse.KakaoAccount;
import com.example.ililbooks.client.kakao.dto.AuthKakaoResponse.Profile;
import com.example.ililbooks.client.kakao.dto.AuthKakaoTokenResponse;
import com.example.ililbooks.domain.auth.service.TokenService;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.LoginType;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.ililbooks.global.exception.ErrorMessage.DEACTIVATED_USER_EMAIL;
import static com.example.ililbooks.global.exception.ErrorMessage.INVALID_USER_INFORMATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthKakaoServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private TokenService tokenService;

    @Mock
    private KakaoClient kakaoClient;

    @InjectMocks
    private AuthKakaoService authKakaoService;

    private final String code = "auth_code";
    private final String accessToken = "access_token";
    private final String refreshToken = "refresh_token";

    private final KakaoAccount kakaoAccount = new KakaoAccount("test@example.com",
                    new Profile("nickname", "profileImageUrl", "thumbnailUrl"),
            true, true, true);

    private final Users user = Users.builder()
            .email("test@example.com")
            .nickname("nickname")
            .loginType(LoginType.KAKAO)
            .userRole(UserRole.ROLE_USER)
            .isDeleted(false)
            .build();

    @Test
    void 정상_카카오_로그인_처리() {
        // given
        Profile profile = new Profile("nickname", "profile_image_url", "thumbnail_url");
        KakaoAccount kakaoAccount = new KakaoAccount("test@example.com", profile, true, true, true);
        AuthKakaoResponse kakaoResponse = new AuthKakaoResponse(12345L, kakaoAccount);

        given(kakaoClient.requestToken(anyString())).willReturn(new AuthKakaoTokenResponse(accessToken, refreshToken));

        given(kakaoClient.requestUserInfo(anyString())).willReturn(kakaoResponse);

        given(userService.findByEmailOrGet(anyString(), anyString(), any(LoginType.class), any(UserRole.class))).willReturn(user);

        // when
        AuthKakaoTokenResponse result = authKakaoService.signIn(code);

        // then
        assertThat(result).isNotNull();
    }

//    @Test
//    void 이메일_또는_닉네임_없을_경우_예외발생() {
//        // given
//        KakaoAccount emptyAccount = new KakaoAccount("",
//                new Profile("", "profileImageUrl", "thumbnailUrl"),
//                false, false, false);
//
//        when(kakaoClient.requestToken(code)).thenReturn(new AuthKakaoTokenResponse(accessToken, refreshToken));
//        when(kakaoClient.requestUserInfo(accessToken)).thenReturn(new AuthKakaoResponse(anyLong(), emptyAccount));
//
//        // then
//        assertThrows(BadRequestException.class, () -> authKakaoService.signIn(code), INVALID_USER_INFORMATION.getMessage());
//    }

    @Test
    void 삭제된_사용자일_경우_예외발생() {
        // given
        Profile profile = new Profile("nickname", "profile_image_url", "thumbnail_url");
        KakaoAccount kakaoAccount = new KakaoAccount("test@example.com", profile, true, true, true);
        AuthKakaoResponse kakaoResponse = new AuthKakaoResponse(12345L, kakaoAccount);

        Users deletedUser = Users.builder()
                .email("test@example.com")
                .nickname("nickname")
                .loginType(LoginType.KAKAO)
                .userRole(UserRole.ROLE_USER)
                .isDeleted(true)
                .build();

        given(kakaoClient.requestToken(code)).willReturn(new AuthKakaoTokenResponse(accessToken, refreshToken));

        given(kakaoClient.requestUserInfo(anyString())).willReturn(kakaoResponse);

        given(userService.findByEmailOrGet(anyString(), anyString(), any(LoginType.class), any(UserRole.class))).willReturn(deletedUser);

        // then
        assertThrows(NotFoundException.class, () -> authKakaoService.signIn(code), DEACTIVATED_USER_EMAIL.getMessage());
    }
}
