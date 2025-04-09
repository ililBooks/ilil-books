package com.example.ililbooks.domain.user.service;

import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.domain.user.repository.UserRepository;
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
public class UsersServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private Users users;

    @BeforeEach
    public void setUp() {
        users = Users.builder()
                .nickname("nickname")
                .userRole(UserRole.ROLE_USER)
                .build();
    }

//    /* findUserByIdOrElseThrow */
//    @Test
//    void findById조회_userId가_없을_경우_실패() {
//        // given
//        Long userId = 1L;
//
//        given(userRepository.findById(anyLong())).willReturn(Optional.empty());
//
//        // when & then
//        NotFoundException notFoundException = assertThrows(NotFoundException.class,
//                () -> userService.findByIdOrElseThrow(userId));
//        assertEquals(notFoundException.getMessage(), USER_ID_NOT_FOUND.getMessage());
//    }
//
//    @Test
//    void findById조회_성공() {
//        // given
//        Long userId = 1L;
//        ReflectionTestUtils.setField(users, "id", userId);
//
//        given(userRepository.findById(anyLong())).willReturn(Optional.of(users));
//
//        // when
//        Users resultUsers = userService.findByIdOrElseThrow(userId);
//
//        // then
//        assertNotNull(resultUsers);
//        assertEquals(users.getId(), resultUsers.getId());
//        assertEquals(users.getNickname(), resultUsers.getNickname());
//        assertEquals(users.getUserRole(), resultUsers.getUserRole());
//    }
//
//    /* findUserByEmailOrElseThrow */
//    @Test
//    void findByEmail조회_email이_없을_경우_실패() {
//        // given
//        String email = "email@email.com";
//
//        given(userRepository.findByEmail(any(String.class))).willReturn(Optional.empty());
//
//        // when & then
//        UnauthorizedException unauthorizedException = assertThrows(UnauthorizedException.class,
//                () -> userService.findByEmailOrElseThrow(email));
//        assertEquals(unauthorizedException.getMessage(), USER_EMAIL_NOT_FOUND.getMessage());
//    }
//
//    @Test
//    void findByEmail조회_성공() {
//        // given
//        String email = "email@email.com";
//        ReflectionTestUtils.setField(users, "email", email);
//
//        given(userRepository.findByEmail(any(String.class))).willReturn(Optional.of(users));
//
//        // when
//        Users resultUsers = userService.findByEmailOrElseThrow(email);
//
//        // then
//        assertNotNull(resultUsers);
//        assertEquals(users.getEmail(), resultUsers.getEmail());
//        assertEquals(users.getNickname(), resultUsers.getNickname());
//        assertEquals(users.getUserRole(), resultUsers.getUserRole());
//    }
//
//    /* saveUser */
//    @Test
//    void 회원저장_중복된_이메일이_있을_경우_실패() {
//        // given
//        String email = "email@email.com";
//        String nickname = "nickname";
//        String password = "password1234";
//        String userRole = "USER_ROLE";
//
//        given(userRepository.existsByEmail(any(String.class))).willReturn(true);
//
//        // when & then
//        BadRequestException badRequestException = assertThrows(BadRequestException.class,
//                () -> userService.saveUser(email, nickname, password, userRole));
//        assertEquals(badRequestException.getMessage(), DUPLICATE_EMAIL.getMessage());
//    }
//
//    @Test
//    void 회원저장_성공() {
//        // given
//        String email = "email@email.com";
//        String nickname = "nickname";
//        String password = "password1234";
//        String userRole = "ROLE_USER";
//        ReflectionTestUtils.setField(users, "email", "email@email.com");
//        ReflectionTestUtils.setField(users, "password", "password1234");
//
//
//        String encodedPassword = "encoded-password1234";
//
//        given(userRepository.existsByEmail(any(String.class))).willReturn(false);
//        given(passwordEncoder.encode(any(String.class))).willReturn(encodedPassword);
//        given(userRepository.save(any(Users.class))).willReturn(users);
//
//        // when
//        Users resultUsers = userService.saveUser(email, nickname, password, userRole);
//
//        // then
//        assertNotNull(resultUsers);
//        assertEquals(email, resultUsers.getEmail());
//        assertEquals(nickname, resultUsers.getNickname());
//        assertEquals(password, resultUsers.getPassword());
//        assertEquals(UserRole.of(userRole), resultUsers.getUserRole());
//
//    }
}
