package com.example.ililbooks.domain.payment.dto.request;

import lombok.Builder;

import java.math.BigDecimal;

public record PaymentVerificationDto(
        String impUid,
        String merchantUid,
        BigDecimal amount
) {
    @Builder
    public PaymentVerificationDto {
    }
}
