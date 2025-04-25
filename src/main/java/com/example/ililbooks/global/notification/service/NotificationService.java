package com.example.ililbooks.global.notification.service;

import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.asynchronous.rabbitmq.dto.request.MessageRequest;
import com.example.ililbooks.global.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final AsyncNotificationService asyncNotificationService;
    private final RabbitMqNotificationService rabbitMqNotificationService;
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

    /**
     * RabbitMQ 비동기 처리
     * consumer (queue에서 데이터를 가져와 메일 발송)
     */
    @RabbitListener(queues = "${spring.rabbitmq.template.default-receive-queue}")
    public void sendOrderMail(MessageRequest messageRequest) {
        rabbitMqNotificationService.sendOrderMail(
                messageRequest.email(),
                messageRequest.nickName(),
                messageRequest.orderNumber(),
                messageRequest.totalPrice()
        );
    }
}
