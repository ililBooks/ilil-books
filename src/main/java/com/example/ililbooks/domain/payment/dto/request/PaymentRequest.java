package com.example.ililbooks.domain.payment.dto.request;

import com.example.ililbooks.domain.payment.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Schema(description = "결제창에 결제 정보를 전달하기 위한 요청 DTO")
public record PaymentRequest(
        @Schema(example = "html5_inicis")
        String pg,

        @Schema(example = "card")
        String payMethod,

        @Schema(example = "merchantUid_e0482c9a")
        String merchantUid,

        @Schema(example = "자바 ORM 표준 JPA 프로그래밍3")
        String name,

        @Schema(example = "10000")
        BigDecimal amount,

        @Schema(example = "email@email.com")
        String buyerEmail,

        @Schema(example = "nickname")
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
