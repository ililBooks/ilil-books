package com.example.ililbooks.domain.limitedreservation.dto.response;

import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LimitedReservationResponse {

    private final Long reservationId;
    private final Long limitedEventId;
    private final Long userId;
    private final LimitedReservationStatus status;
    private final LocalDateTime expiredAt;

    @Builder
    public LimitedReservationResponse(Long reservationId, Long limitedEventId, Long userId, LimitedReservationStatus status, LocalDateTime expiredAt) {
        this.reservationId = reservationId;
        this.limitedEventId = limitedEventId;
        this.userId = userId;
        this.status = status;
        this.expiredAt = expiredAt;
    }

    public static LimitedReservationResponse of(LimitedReservation reservation) {
        return LimitedReservationResponse.builder()
                .reservationId(reservation.getId())
                .limitedEventId(reservation.getLimitedEvent().getId())
                .userId(reservation.getUsers().getId())
                .status(reservation.getStatus())
                .expiredAt(reservation.getExpiredAt())
                .build();
    }
}
