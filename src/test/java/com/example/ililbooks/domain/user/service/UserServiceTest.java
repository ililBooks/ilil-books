package com.example.ililbooks.domain.user.service;

import com.example.ililbooks.config.util.JwtUtil;
import com.example.ililbooks.domain.auth.dto.request.AuthSignUpRequest;
import com.example.ililbooks.domain.auth.dto.response.AuthAccessTokenResponse;
import com.example.ililbooks.domain.user.dto.request.UserDeleteRequest;
import com.example.ililbooks.domain.user.dto.request.UserUpdatePasswordRequest;
import com.example.ililbooks.domain.user.dto.request.UserUpdateRequest;
import com.example.ililbooks.domain.user.dto.response.UserResponse;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.LoginType;
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

import java.util.Optional;

import static com.example.ililbooks.global.exception.ErrorMessage.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private AuthSignUpRequest authSignUpRequest;
    private UserUpdateRequest userUpdateRequest;
    private UserUpdatePasswordRequest wrongCheckPasswordUpdateRequest;
    private UserUpdatePasswordRequest wrongPasswordUpdateRequest;
    private UserUpdatePasswordRequest successPasswordUpdateRequest;
    private UserDeleteRequest userDeleteRequest;
    private Users users;
    private AuthUser authUser;

    @BeforeEach
    public void setUp() {
        authSignUpRequest = AuthSignUpRequest.builder()
                .email("email@email.com")
                .nickname("nickname")
                .password("password1234")
                .userRole("ROLE_USER")
                .build();

        userUpdateRequest = UserUpdateRequest.builder()
                .nickname("nickname")
                .zipCode("12345")
                .roadAddress("도로명주소")
                .detailedAddress("상세주소")
                .contactNumber("01012345678")
                .build();

        wrongCheckPasswordUpdateRequest = UserUpdatePasswordRequest.builder()
                .oldPassword("password1234")
                .newPassword("new-password")
                .newPasswordCheck("wrong-password")
                .build();

        wrongPasswordUpdateRequest = UserUpdatePasswordRequest.builder()
                .oldPassword("wrong-password")
                .newPassword("new-password")
                .newPasswordCheck("new-password")
                .build();

        successPasswordUpdateRequest = UserUpdatePasswordRequest.builder()
                .oldPassword("old-password")
                .newPassword("new-password")
                .newPasswordCheck("new-password")
                .build();

        userDeleteRequest = UserDeleteRequest.builder()
                .password("wrong-password")
                .build();

        users = Users.builder()
                .id(1L)
                .email("email@email.com")
                .nickname("nickname")
                .password("encoded-password")
                .zipCode("12345")
                .roadAddress("도로명주소")
                .detailedAddress("상세주소")
                .contactNumber("01012345678")
                .loginType(LoginType.EMAIL)
                .userRole(UserRole.ROLE_USER)
                .build();

        authUser = AuthUser.builder()
                .userId(1L)
                .email("email@email.com")
                .nickname("nickname")
                .role(UserRole.ROLE_USER)
                .build();
    }

    /* saveUser */
    @Test
    void 회원_저장_중복_이메일_가입_실패() {
        // given
        given(userRepository.existsByEmailAndLoginType(anyString(), any(LoginType.class))).willReturn(true);

        // when & then
        BadRequestException BadRequestException = assertThrows(BadRequestException.class,
                () -> userService.saveUser(authSignUpRequest));
        assertEquals(BadRequestException.getMessage(), DUPLICATE_EMAIL.getMessage());
    }

    @Test
    void 회원_저장_성공() {
        // given
        given(userRepository.existsByEmailAndLoginType(anyString(), any(LoginType.class))).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn(users.getPassword());
        given(userRepository.save(any(Users.class))).willReturn(users);

        // when
        Users result = userService.saveUser(authSignUpRequest);

        // then
        assertEquals(users.getEmail(), result.getEmail());
        assertEquals(users.getNickname(), result.getNickname());
        assertEquals(users.getPassword(), result.getPassword());
        assertEquals(users.getUserRole(), result.getUserRole());
        verify(userRepository, times(1)).save(any(Users.class));
    }

    /* findUser */
    @Test
    void 회원_조회_성공() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(users));

        // when
        UserResponse result = userService.findUser(authUser);

        // then
        assertEquals(users.getId(), result.id());
        assertEquals(users.getEmail(), result.email());
        assertEquals(users.getNickname(), result.nickname());
        assertEquals(users.getZipCode(), result.zipCode());
        assertEquals(users.getRoadAddress(), result.roadAddress());
        assertEquals(users.getDetailedAddress(), result.detailedAddress());
        assertEquals(users.getContactNumber(), result.contactNumber());
        assertEquals(users.getLoginType().name(), result.loginType());
        verify(userRepository).findById(anyLong());
    }

    /* updateUser */
    @Test
    void 회원_수정_성공() {
        // given
        String expectedToken = "new-access-token";
        Users mockUsers = mock(Users.class);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(mockUsers));
        given(jwtUtil.createAccessToken(mockUsers.getId(), mockUsers.getEmail(), mockUsers.getNickname(), mockUsers.getUserRole())).willReturn(expectedToken);

        // when
        AuthAccessTokenResponse result = userService.updateUser(authUser, userUpdateRequest);

        // then
        assertEquals(expectedToken, result.accessToken());
        verify(mockUsers, times(1)).updateUser(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    /* updatePasswordUser */
    @Test
    void 회원_비밀번호_수정_비밀번호_확인_불일치로_실패() {
        // given

        // when & then
        BadRequestException BadRequestException = assertThrows(BadRequestException.class,
                () -> userService.updatePasswordUser(authUser, wrongCheckPasswordUpdateRequest));
        assertEquals(BadRequestException.getMessage(), PASSWORD_CONFIRMATION_MISMATCH.getMessage());
    }

    @Test
    void 회원_비밀번호_수정_비밀번호가_맞지않아_실패() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(users));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // when & then
        BadRequestException BadRequestException = assertThrows(BadRequestException.class,
                () -> userService.updatePasswordUser(authUser, wrongPasswordUpdateRequest));
        assertEquals(BadRequestException.getMessage(), INVALID_PASSWORD.getMessage());
    }

    @Test
    void 회원_비밀번호_수정_성공() {
        // given
        Users mockUsers = mock(Users.class);
        String encodedNewPassword = "encoded-new-password";

        given(userRepository.findById(anyLong())).willReturn(Optional.of(mockUsers));
        given(passwordEncoder.matches(successPasswordUpdateRequest.oldPassword(), mockUsers.getPassword())).willReturn(true);
        given(passwordEncoder.encode(anyString())).willReturn(encodedNewPassword);

        // when
        userService.updatePasswordUser(authUser, successPasswordUpdateRequest);

        // then
        verify(mockUsers, times(1)).updatePassword(encodedNewPassword);
    }

    /* deleteUser */
    @Test
    void 회원_삭제_비밀번호_불일치로_실패() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.of(users));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // when & then
        BadRequestException BadRequestException = assertThrows(BadRequestException.class,
                () -> userService.deleteUser(authUser, userDeleteRequest));
        assertEquals(BadRequestException.getMessage(), INVALID_PASSWORD.getMessage());
    }

    @Test
    void 회원_삭제_성공() {
        // given
        Users mockUsers = mock(Users.class);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(mockUsers));
        given(passwordEncoder.matches(userDeleteRequest.password(), mockUsers.getPassword())).willReturn(true);

        // when
        userService.deleteUser(authUser, userDeleteRequest);

        // then
        verify(mockUsers, times(1)).deleteUser();
    }

    /* findByEmailOrElseThrow */
    @Test
    void 이메일로_유저_조회_실패() {
        // when
        String email = "email@email.com";
        given(userRepository.findByEmailAndLoginType(anyString(), any(LoginType.class))).willReturn(Optional.empty());

        // when & then
        UnauthorizedException unauthorizedException = assertThrows(UnauthorizedException.class,
                () -> userService.findByEmailAndLoginTypeOrElseThrow(email, LoginType.EMAIL));
        assertEquals(unauthorizedException.getMessage(), USER_EMAIL_NOT_FOUND.getMessage());
    }

    @Test
    void 이메일로_유저_조회_성공() {
        // when
        String email = "email@email.com";
        given(userRepository.findByEmailAndLoginType(anyString(), any(LoginType.class))).willReturn(Optional.of(users));

        // when
        Users result = userService.findByEmailAndLoginTypeOrElseThrow(email, LoginType.EMAIL);

        // given
        assertEquals(users.getEmail(), result.getEmail());
        assertEquals(users.getNickname(), result.getNickname());
        assertEquals(users.getUserRole(), result.getUserRole());
        verify(userRepository, times(1)).findByEmailAndLoginType(anyString(), any(LoginType.class));
    }

    /* findByIdOrElseThrow */
    @Test
    void id로_유저_조회_실패() {
        // when
        Long userId = 1L;
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> userService.findByIdOrElseThrow(userId));
        assertEquals(notFoundException.getMessage(), USER_ID_NOT_FOUND.getMessage());
    }

    @Test
    void id로_유저_조회_성공() {
        // when
        Long userId = 1L;
        given(userRepository.findById(anyLong())).willReturn(Optional.of(users));

        // when
        Users result = userService.findByIdOrElseThrow(userId);

        // given
        assertEquals(users.getEmail(), result.getEmail());
        assertEquals(users.getNickname(), result.getNickname());
        assertEquals(users.getUserRole(), result.getUserRole());
        verify(userRepository, times(1)).findById(userId);
    }

    /* existsByEmail */
    @Test
    void existsByEmail_존재함_성공() {
        // given
        String email = "test@example.com";
        given(userRepository.existsByEmailAndLoginType(anyString(), any(LoginType.class))).willReturn(true);

        // when
        boolean result = userService.existsByEmailAndLoginType(email, LoginType.EMAIL);

        // then
        assertTrue(result);
        verify(userRepository, times(1)).existsByEmailAndLoginType(anyString(), any(LoginType.class));
    }

    @Test
    void existsByEmail_존재하지_않음_실패() {
        // given
        String email = "notfound@example.com";
        given(userRepository.existsByEmailAndLoginType(anyString(), any(LoginType.class))).willReturn(false);

        // when
        boolean result = userService.existsByEmailAndLoginType(email, LoginType.EMAIL);

        // then
        assertFalse(result);
        verify(userRepository, times(1)).existsByEmailAndLoginType(anyString(), any(LoginType.class));
    }
}