package com.example.ililbooks.domain.payment.dto.request;

import lombok.Builder;

import java.math.BigDecimal;

public record PaymentVerificationRequest(
        String impUid,
        String merchantUid,
        BigDecimal amount
) {
    @Builder
    public PaymentVerificationRequest {
    }
}
