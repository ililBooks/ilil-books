package com.example.ililbooks.domain.limitedreservation.dto.response;

import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;

import java.time.Instant;

public record LimitedReservationResponse(
        Long reservationId,
        Long limitedEventId,
        Long userId,
        LimitedReservationStatus status,
        Instant expiredAt
) {
    public static LimitedReservationResponse of(LimitedReservation reservation) {
        return new LimitedReservationResponse(
                reservation.getId(),
                reservation.getLimitedEvent().getId(),
                reservation.getUsers().getId(),
                reservation.getStatus(),
                reservation.getExpiredAt()
        );
    }
}