package com.example.ililbooks.domain.limitedreservation.dto.response;

import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import lombok.Builder;

import java.time.Instant;

public record LimitedReservationStatusResponse(
        Long reservationId,
        String eventTitle,
        LimitedReservationStatus status,
        Instant expiresAt
) {

    @Builder
    public LimitedReservationStatusResponse {
    }

    public static LimitedReservationStatusResponse of(LimitedReservation reservation) {
        return LimitedReservationStatusResponse.builder()
                .reservationId(reservation.getId())
                .eventTitle(reservation.getLimitedEvent().getTitle())
                .status(reservation.getStatus())
                .expiresAt(reservation.getExpiresAt())
                .build();
    }
}
