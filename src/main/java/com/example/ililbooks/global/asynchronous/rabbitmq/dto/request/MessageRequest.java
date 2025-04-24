package com.example.ililbooks.global.asynchronous.rabbitmq.dto.request;

import java.math.BigDecimal;

public record MessageRequest(
        String email,
        String nickName,
        String  orderNumber,
        BigDecimal totalPrice
){

    public static MessageRequest of(String email, String nickName, String orderNumber, BigDecimal totalPrice) {
        return new MessageRequest(
                email,
                nickName,
                orderNumber,
                totalPrice
        );
    }
}
