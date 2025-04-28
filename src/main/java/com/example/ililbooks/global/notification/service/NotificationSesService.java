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

    private final SesClient sesClient;
    private final Environment env;

    /**
     * Ses를 통해 메일 전송
     */
    public void sendOrderMail(AuthUser authUser, String orderNumber, BigDecimal totalPrice) {
        SendRequest sendRequest = SendRequest.builder()
                .from(env.getProperty("spring.mail.username"))
                .subject("주문이 완료되었습니다.")
                .to(authUser.getEmail())
                .content("주문 정보\n" +
                        "-------------------------\n" +
                        "닉네임: " + authUser.getNickname() + "\n" +
                        "주문 번호: " + orderNumber + "\n" +
                        "총 가격: " + totalPrice + "\n")
                .build();

        sesClient.sendEmail(sendRequest.toSendEmailRequest());
    }
}
