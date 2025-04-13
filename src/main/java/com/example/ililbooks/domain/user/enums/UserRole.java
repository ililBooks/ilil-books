package com.example.ililbooks.domain.user.enums;

import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.UnauthorizedException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.PUBLISHER;
import static com.example.ililbooks.domain.user.enums.UserRole.Authority.USER;

@Getter
@RequiredArgsConstructor
public enum UserRole implements GrantedAuthority {

    ROLE_USER(Authority.USER),
    ROLE_PUBLISHER(Authority.PUBLISHER),
    ROLE_ADMIN(Authority.ADMIN);

    private final String userRole;

    public static UserRole of(String role) {
        return Arrays.stream(UserRole.values())
                .filter(r -> r.getUserRole().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("유효하지 않은 UserRole입니다."));
    }

    @Override
    public String getAuthority() {
        return userRole;
    }

    public static class Authority {
        public static final String USER = "ROLE_USER";
        public static final String PUBLISHER = "ROLE_PUBLISHER";
        public static final String ADMIN = "ROLE_ADMIN";
    }

    public static boolean isPublisher(AuthUser authUser) {
        return authUser.getAuthorities().stream()
                .anyMatch(auth -> PUBLISHER.equals(auth.getAuthority()));
    }

    public static boolean isUser(AuthUser authUser) {
        return authUser.getAuthorities().stream()
                .anyMatch(auth -> USER.equals(auth.getAuthority()));
    }
}
