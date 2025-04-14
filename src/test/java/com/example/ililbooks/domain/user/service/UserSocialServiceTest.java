package com.example.ililbooks.domain.user.service;

import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSocialServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSocialService userSocialService;

    /* saveUser */
    @Test
    void 유저_저장_성공() {
        // given
        Users mockUsers = mock(Users.class);
        given(userRepository.save(mockUsers)).willReturn(mockUsers);

        // when
        Users result = userSocialService.saveUser(mockUsers);

        // then
        Assertions.assertEquals(mockUsers.getEmail(), result.getEmail());
        Assertions.assertEquals(mockUsers.getNickname(), result.getNickname());
        Assertions.assertEquals(mockUsers.getUserRole(), result.getUserRole());
        verify(userRepository, times(1)).save(mockUsers);
    }
}