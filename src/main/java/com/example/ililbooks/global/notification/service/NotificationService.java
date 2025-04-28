package com.example.ililbooks.global.notification.service;

import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.asynchronous.rabbitmq.dto.request.MessagePromotionRequest;
import com.example.ililbooks.global.asynchronous.rabbitmq.dto.request.MessageOrderRequest;
import com.example.ililbooks.global.asynchronous.rabbitmq.service.RabbitMqService;
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
    private final NotificationAsyncService notificationAsyncService;
    private final NotificationRabbitMqService notificationRabbitMqService;
    private final RabbitMqService rabbitMqService;
    private final UserService userService;

    // @Async 비동기 처리
    @Transactional(readOnly = true)
    public void sendPromotionEmail() {
        List<Users> users = userService.findAllByNotificationAgreed();

        users.forEach(user ->
                        notificationAsyncService.sendPromotionEmail(user.getEmail(), user.getNickname())
                );
    }

    // @Async 비동기 처리
    public void sendOrderMail(AuthUser authUser, String orderNumber, BigDecimal price) {
        notificationAsyncService.sendOrderMail(authUser, orderNumber, price);
    }

    /**
     * Consumer (queue에서 데이터를 가져와 메일 발송)
     * RabbitMQ 비동기 처리
     */
    @RabbitListener(queues = "order-mail-queue")
    public void sendOrderMail(MessageOrderRequest messageOrderRequest) {
        notificationRabbitMqService.sendOrderMail(
                messageOrderRequest.email(),
                messageOrderRequest.nickName(),
                messageOrderRequest.orderNumber(),
                messageOrderRequest.totalPrice()
        );
    }

    /**
     * RabbitMQ 비동기 처리
     */
    @Transactional(readOnly = true)
    public void sendPromotionEmailWithRabbitMq() {
        List<Users> users = userService.findAllByNotificationAgreed();

        users.forEach(user ->
                rabbitMqService.sendPromotionMessage(MessagePromotionRequest.of(user.getEmail(), user.getNickname()))
        );
    }
}

