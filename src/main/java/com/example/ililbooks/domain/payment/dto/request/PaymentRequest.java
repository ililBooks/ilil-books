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
        String buyEmail,
        String buyName
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
                .buyEmail(payment.getBuyerEmail())
                .buyName(payment.getBuyerName())
                .build();
    }
}
