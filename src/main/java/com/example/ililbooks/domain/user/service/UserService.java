package com.example.ililbooks.domain.user.service;

import com.example.ililbooks.domain.user.entity.User;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.domain.user.repository.UserRepository;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /* 회원저장 */
    @Transactional
    public User saveUser(String email, String nickname, String password, String userRole) {

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException(DUPLICATE_EMAIL.getMessage());
        }

        String encodedPassword = passwordEncoder.encode(password);

        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .password(encodedPassword)
                .userRole(UserRole.of(userRole))
                .build();

        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UnauthorizedException(USER_EMAIL_NOT_FOUND.getMessage())
        );
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(USER_ID_NOT_FOUND.getMessage())
        );
    }
}
