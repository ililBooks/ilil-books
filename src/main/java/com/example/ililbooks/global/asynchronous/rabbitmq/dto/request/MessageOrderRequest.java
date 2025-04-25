package com.example.ililbooks.global.asynchronous.rabbitmq.dto.request;

import java.math.BigDecimal;

public record MessageOrderRequest(
        String email,
        String nickName,
        String  orderNumber,
        BigDecimal totalPrice
){

    public static MessageOrderRequest of(String email, String nickName, String orderNumber, BigDecimal totalPrice) {
        return new MessageOrderRequest(
                email,
                nickName,
                orderNumber,
                totalPrice
        );
    }
}
