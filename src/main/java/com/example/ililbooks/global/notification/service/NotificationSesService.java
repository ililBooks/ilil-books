package com.example.ililbooks.global.notification.service;

import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.notification.dto.request.SendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class NotificationSesService {

    private final NotificationAsyncService notificationAsyncService;

    // @Async 비동기 처리
    public void sendOrderMailWithAsync(AuthUser authUser, String orderNumber, BigDecimal price) {
        notificationAsyncService.sendOrderMailWithSes(authUser, orderNumber, price);
    }
}
