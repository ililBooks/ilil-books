package com.example.ililbooks.domain.limitedreservation.dto.response;

import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LimitedReservationResponse {

    private final Long reservationId;
    private final Long limitedEventId;
    private final Long userId;
    private final LimitedReservationStatus status;
    private final LocalDateTime expiredAt;

    public static LimitedReservationResponse from(LimitedReservation reservation) {
        return LimitedReservationResponse.builder()
                .reservationId(reservation.getId())
                .limitedEventId(reservation.getLimitedEvent().getId())
                .userId(reservation.getUsers().getId())
                .status(reservation.getStatus())
                .expiredAt(reservation.getExpiredAt())
                .build();
    }
}
