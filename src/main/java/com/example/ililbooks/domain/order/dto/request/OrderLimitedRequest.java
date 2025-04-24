package com.example.ililbooks.domain.order.dto.request;

import jakarta.validation.constraints.NotNull;

import static com.example.ililbooks.global.dto.ValidationMessage.NOT_NULL_RESERVATION_ID;

public record OrderLimitedRequest(
        @NotNull(message = NOT_NULL_RESERVATION_ID)
        Long reservationId
) {
}
