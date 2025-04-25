package com.example.ililbooks.domain.payment.dto.response;

import com.example.ililbooks.domain.payment.entity.Payment;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        String impUid,
        String merchantUid,
        String pg,
        String paymentMethod,
        String buyerEmail,
        String buyerName,
        BigDecimal amount,
        String payStatus,
        Instant paidAt
) {
    @Builder
    public PaymentResponse {
    }

    public static PaymentResponse of(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrder().getId())
                .impUid(payment.getImpUid())
                .merchantUid(payment.getMerchantUid())
                .pg(payment.getPg().getName())
                .paymentMethod(payment.getPaymentMethod().getName())
                .buyerEmail(payment.getBuyerEmail())
                .buyerName(payment.getBuyerName())
                .amount(payment.getAmount())
                .payStatus(payment.getPayStatus().name())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
