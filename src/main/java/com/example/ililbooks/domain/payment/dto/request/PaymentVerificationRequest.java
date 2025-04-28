package com.example.ililbooks.domain.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Schema(description = "결제 정보를 검증하기 위한 DTO")
public record PaymentVerificationRequest(
        @Schema(example = "imp_491348408942")
        String impUid,

        @Schema(example = "merchantUid_e0482c9a")
        String merchantUid,

        @Schema(example = "10000")
        BigDecimal amount
) {
    @Builder
    public PaymentVerificationRequest {
    }
}
