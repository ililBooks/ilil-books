package com.example.ililbooks.global.notification.service;

import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final AsyncNotificationService asyncNotificationService;
    private final UserService userService;

    // @Async 비동기 처리
    @Transactional(readOnly = true)
    public void sendPromotionEmail() {
        List<Users> users = userService.findAllByNotificationAgreed();

        users.forEach(user ->
                        asyncNotificationService.sendPromotionEmail(user.getEmail(), user.getNickname())
                );
    }

    // @Async 비동기 처리
    public void sendOrderMail(AuthUser authUser, String orderNumber, BigDecimal price) {
        asyncNotificationService.sendOrderMail(authUser, orderNumber, price);
    }
}
