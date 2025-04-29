package com.example.ililbooks.global.notification.controller;

import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import com.example.ililbooks.global.notification.service.NotificationSesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/notification")
public class NotificationSesController {

    private final NotificationSesService notificationSesService;

    @GetMapping("/send-order")
    public Response<Void> sendOrderMail(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        notificationSesService.sendOrderMail(authUser, "order number - 123", BigDecimal.valueOf(10000));
//        for (int i = 0; i < 100; i++) {
//        }
        return Response.empty();
    }
}
