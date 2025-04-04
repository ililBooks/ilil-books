package com.example.ililbooks.domain.user.service;

import com.example.ililbooks.domain.user.entity.User;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.domain.user.repository.UserRepository;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.global.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.example.ililbooks.global.exception.ErrorMessage.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .nickname("nickname")
                .userRole(UserRole.ROLE_USER)
                .build();
    }

    /* findUserByIdOrElseThrow */
    @Test
    void findById조회_userId가_없을_경우_실패() {
        // given
        Long userId = 1L;

        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> userService.getUserById(userId));
        assertEquals(notFoundException.getMessage(), USER_ID_NOT_FOUND.getMessage());
    }

    @Test
    void findById조회_성공() {
        // given
        Long userId = 1L;
        ReflectionTestUtils.setField(user, "id", userId);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        // when
        User resultUser = userService.getUserById(userId);

        // then
        assertNotNull(resultUser);
        assertEquals(user.getId(), resultUser.getId());
        assertEquals(user.getNickname(), resultUser.getNickname());
        assertEquals(user.getUserRole(), resultUser.getUserRole());
    }

    /* findUserByEmailOrElseThrow */
    @Test
    void findByEmail조회_email이_없을_경우_실패() {
        // given
        String email = "email@email.com";

        given(userRepository.findByEmail(any(String.class))).willReturn(Optional.empty());

        // when & then
        UnauthorizedException unauthorizedException = assertThrows(UnauthorizedException.class,
                () -> userService.getUserByEmail(email));
        assertEquals(unauthorizedException.getMessage(), USER_EMAIL_NOT_FOUND.getMessage());
    }

    @Test
    void findByEmail조회_성공() {
        // given
        String email = "email@email.com";
        ReflectionTestUtils.setField(user, "email", email);

        given(userRepository.findByEmail(any(String.class))).willReturn(Optional.of(user));

        // when
        User resultUser = userService.getUserByEmail(email);

        // then
        assertNotNull(resultUser);
        assertEquals(user.getEmail(), resultUser.getEmail());
        assertEquals(user.getNickname(), resultUser.getNickname());
        assertEquals(user.getUserRole(), resultUser.getUserRole());
    }

    /* saveUser */
    @Test
    void 회원저장_중복된_이메일이_있을_경우_실패() {
        // given
        String email = "email@email.com";
        String nickname = "nickname";
        String password = "password1234";
        String userRole = "USER_ROLE";

        given(userRepository.existsByEmail(any(String.class))).willReturn(true);

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> userService.saveUser(email, nickname, password, userRole));
        assertEquals(badRequestException.getMessage(), DUPLICATE_EMAIL.getMessage());
    }

    @Test
    void 회원저장_성공() {
        // given
        String email = "email@email.com";
        String nickname = "nickname";
        String password = "password1234";
        String userRole = "ROLE_USER";
        ReflectionTestUtils.setField(user, "email", "email@email.com");
        ReflectionTestUtils.setField(user, "password", "password1234");


        String encodedPassword = "encoded-password1234";

        given(userRepository.existsByEmail(any(String.class))).willReturn(false);
        given(passwordEncoder.encode(any(String.class))).willReturn(encodedPassword);
        given(userRepository.save(any(User.class))).willReturn(user);

        // when
        User resultUser = userService.saveUser(email, nickname, password, userRole);

        // then
        assertNotNull(resultUser);
        assertEquals(email, resultUser.getEmail());
        assertEquals(nickname, resultUser.getNickname());
        assertEquals(password, resultUser.getPassword());
        assertEquals(UserRole.of(userRole), resultUser.getUserRole());

    }
}
