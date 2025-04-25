package com.example.ililbooks.global.notification.controller;

import com.example.ililbooks.global.asynchronous.rabbitmq.dto.request.MessagePromotionRequest;
import com.example.ililbooks.global.asynchronous.rabbitmq.service.RabbitMqService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import com.example.ililbooks.global.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class TestEmailController {

    private final NotificationService notificationService;
    private final RabbitMqService rabbitMqService;

    @GetMapping("/send-test-email")
    public Response<Void> sendTest(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        notificationService.sendOrderMail(authUser, "order number - 123", BigDecimal.valueOf(10000));
        return Response.empty();
    }

    @GetMapping("/send-promotion/async")
    public Response<Void> send() {
        notificationService.sendPromotionEmail();
        return Response.empty();
    }

    @GetMapping("/send-promotion/rabbitmq")
    public Response<Void> sendPromotionWithRabbitMq(
    ) {
        notificationService.sendPromotionEmailWithRabbitMq();
        return Response.empty();
    }
}