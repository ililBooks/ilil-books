package com.example.ililbooks.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import static com.example.ililbooks.global.dto.ValidationMessage.NOT_NULL_RESERVATION_ID;

@Schema(description = "한정판 예약 Id를 전달하는 DTO")
public record OrderLimitedRequest(
        @Schema(example = "1")
        @NotNull(message = NOT_NULL_RESERVATION_ID)
        Long reservationId
) {
}
