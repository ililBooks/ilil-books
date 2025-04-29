package com.example.ililbooks.domain.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import static com.example.ililbooks.global.dto.ValidationMessage.NOT_NULL_ORDER_ID;

@Schema(description = "주문 Id를 전달하는 DTO")
public record PaymentOrderRequest(
        @Schema(example = "1")
        @NotNull(message = NOT_NULL_ORDER_ID)
        Long orderId
) {
}
