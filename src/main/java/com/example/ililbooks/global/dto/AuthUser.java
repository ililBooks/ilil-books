package com.example.ililbooks.global.dto;

import com.example.ililbooks.domain.user.enums.UserRole;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Getter
public class AuthUser {

    private final Long userId;
    private final String email;
    private final String nickname;
    private final Collection<? extends GrantedAuthority> authorities;

    @Builder
    public AuthUser(Long userId, String email, String nickname, UserRole role) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.authorities = List.of(new SimpleGrantedAuthority(role.name()));
    }
}
