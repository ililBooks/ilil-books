package com.example.ililbooks.domain.auth.kakao.OAuth2User;

import com.example.ililbooks.domain.user.entity.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

public class KakaoOAuth2User extends DefaultOAuth2User {

    private final Users user;

    public KakaoOAuth2User(Users user, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
        super(authorities, attributes, "email");
        this.user = user;
    }

    public Users getUser() {
        return user;
    }

}