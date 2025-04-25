package com.example.ililbooks.domain.payment.dto.request;

import com.example.ililbooks.domain.payment.entity.Payment;
import lombok.Builder;

import java.math.BigDecimal;

public record PaymentRequest(
        String pg,
        String payMethod,
        String merchantUid,
        String name,
        BigDecimal amount,
        String buyerEmail,
        String buyerName
) {
    @Builder
    public PaymentRequest {
    }

    public static PaymentRequest of(Payment payment, String name){
        return PaymentRequest.builder()
                .pg(payment.getPg().getName())
                .payMethod(payment.getPaymentMethod().getName())
                .merchantUid(payment.getMerchantUid())
                .name(name)
                .amount(payment.getAmount())
                .buyerEmail(payment.getBuyerEmail())
                .buyerName(payment.getBuyerName())
                .build();
    }
}
