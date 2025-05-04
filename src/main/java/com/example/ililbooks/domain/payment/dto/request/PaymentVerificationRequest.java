package com.example.ililbooks.domain.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

import static com.example.ililbooks.global.dto.ValidationMessage.*;

@Schema(description = "결제 정보를 검증하기 위한 DTO")
public record PaymentVerificationRequest(
        @Schema(example = "imp_491348408942")
        @NotBlank(message = NOT_BLANK_IMP_UID)
        String impUid,

        @Schema(example = "merchantUid_e0482c9a")
        @NotBlank(message = NOT_BLANK_MERCHANT_UID)
        String merchantUid,

        @Schema(example = "10000")
        @NotNull(message = NOT_NULL_AMOUNT)
        BigDecimal amount
) {
    @Builder
    public PaymentVerificationRequest {
    }
}
