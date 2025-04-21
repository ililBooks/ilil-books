package com.example.ililbooks.domain.auth.kakao.service;

import com.example.ililbooks.client.kakao.KakaoClient;
import com.example.ililbooks.client.kakao.dto.AuthKakaoResponse.KakaoAccount;
import com.example.ililbooks.client.kakao.dto.AuthKakaoTokenResponse;
import com.example.ililbooks.domain.auth.service.TokenService;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.ililbooks.domain.user.enums.LoginType.KAKAO;
import static com.example.ililbooks.global.exception.ErrorMessage.DEACTIVATED_USER_EMAIL;
import static com.example.ililbooks.global.exception.ErrorMessage.INVALID_USER_INFORMATION;

@Service
@RequiredArgsConstructor
public class AuthKakaoService {

    private final UserService userService;
    private final TokenService tokenService;
    private final KakaoClient kakaoClient;

    public AuthKakaoTokenResponse signInWithKakao(String code) {
        // 인가 토큰 받기
        AuthKakaoTokenResponse tokenResponse = kakaoClient.requestToken(code);

        // 사용자 정보 조회
        KakaoAccount kakaoAccount = kakaoClient.requestUserInfo(tokenResponse.accessToken()).kakaoAccount();

        // 사용자 검증 후 부족한 값 있을 경우 검증
        if (kakaoAccount.email().isBlank() || kakaoAccount.profile().nickname().isBlank()) {
            throw new BadRequestException(INVALID_USER_INFORMATION.getMessage());
        }

        // 사용자 정보 프로젝트에 저장 또는 있을 경우 반환
        Users user = userService.findByEmailOrGet(kakaoAccount.email(), kakaoAccount.profile().nickname(), KAKAO, UserRole.ROLE_USER);

        // 삭제된 유저일 경우 예외 처리
        if (user.isDeleted()) {
            throw new NotFoundException(DEACTIVATED_USER_EMAIL.getMessage());
        }

        // 토큰 발급
        String accessToken = tokenService.createAccessToken(user);
        String refreshToken = tokenService.createRefreshToken(user);

        return new AuthKakaoTokenResponse(accessToken, refreshToken);
    }
}
