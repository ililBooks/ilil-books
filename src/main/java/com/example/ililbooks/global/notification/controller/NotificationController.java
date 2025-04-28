package com.example.ililbooks.global.notification.controller;

import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import com.example.ililbooks.global.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/send-order/async")
    public Response<Void> sendOrderMail(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        notificationService.sendOrderMail(authUser, "order number - 123", BigDecimal.valueOf(10000));
        return Response.empty();
    }

    @GetMapping("/send-promotion/async")
    public Response<Void> sendPromotionWithAsync() {
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