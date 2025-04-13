package com.example.ililbooks.domain.auth.kakao.service;

import com.example.ililbooks.domain.auth.entity.RefreshToken;
import com.example.ililbooks.domain.auth.kakao.OAuth2User.KakaoOAuth2User;
import com.example.ililbooks.domain.auth.kakao.dto.request.KakaoOAuth2TokenRequest;
import com.example.ililbooks.domain.auth.kakao.dto.response.KakaoOAuth2TokenResponse;
import com.example.ililbooks.domain.auth.service.TokenService;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class KakaoOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;
    private final TokenService tokenService;
    private final TokenValidator tokenValidator;
    private final WebClient kakaoWebClient;

    /*
     * OAuth2 서비스 구현 메서드
     * Kakao API 에서 사용자 정보를 읽고 local DB에 값이 없으면 저장 후 사용자 정보를 반환한다
     * @Param OAuth2UserRequest
     * @Return OAuth2user
     * */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Kakao 응답 구조
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String email = (String) kakaoAccount.get("email");
        String nickname = (String) profile.get("nickname");

        Users user = userService.findByEmailOrGet(email, nickname);

        return new KakaoOAuth2User(user, Set.of(user.getUserRole()), attributes);
    }

//    /* kakao api 리다이렉트 메서드 */
//    public URI login() {
//        return URI.create("https://kauth.kakao.com/oauth/authorize");
//    }

    /* refresh token 발급 메서드 */
    public KakaoOAuth2TokenResponse refreshToken(KakaoOAuth2TokenRequest kakaoOAuth2TokenRequest) {
        String refreshToken = kakaoOAuth2TokenRequest.refreshToken();

        RefreshToken storedToken = tokenService.findRefreshToken(refreshToken);

        tokenValidator.validateRefreshToken(refreshToken);

        Users user = userService.findByIdOrElseThrow(storedToken.getUserId());
        String newAccessToken = tokenService.createAccessToken(user);
        String newRefreshToken = tokenService.createRefreshToken(user);

        return new KakaoOAuth2TokenResponse(newAccessToken, newRefreshToken);
    }
}
