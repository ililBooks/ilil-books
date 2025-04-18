package com.example.ililbooks.domain.user.service;

import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSocialService {

    private final UserRepository userRepository;

    @Transactional
    public Users saveUser(Users users) {
        return userRepository.save(users);
    }
}
