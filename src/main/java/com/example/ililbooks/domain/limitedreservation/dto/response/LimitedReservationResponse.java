package com.example.ililbooks.domain.limitedreservation.dto.response;

import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import lombok.Builder;

import java.time.Instant;

public record LimitedReservationResponse(
        Long reservationId,
        Long limitedEventId,
        Long userId,
        LimitedReservationStatus status,
        Instant expiresAt,
        Long orderId
) {
    @Builder
    public LimitedReservationResponse {}

    public static LimitedReservationResponse of(LimitedReservation reservation) {
        return LimitedReservationResponse.builder()
                .reservationId(reservation.getId())
                .limitedEventId(reservation.getLimitedEvent().getId())
                .userId(reservation.getUsers().getId())
                .status(reservation.getStatus())
                .expiresAt(reservation.getExpiresAt())
                .orderId(reservation.hasOrder() ? reservation.getOrder().getId() : null)
                .build();
    }
}