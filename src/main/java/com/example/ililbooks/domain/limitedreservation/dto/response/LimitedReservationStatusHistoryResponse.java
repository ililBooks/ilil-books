package com.example.ililbooks.domain.limitedreservation.dto.response;

import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservationStatusHistory;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import lombok.Builder;

import java.time.Instant;

public record LimitedReservationStatusHistoryResponse(
        Long historyId,
        Long reservationId,
        LimitedReservationStatus fromStatus,
        LimitedReservationStatus toStatus,
        Instant changedAt
) {
    @Builder
    public LimitedReservationStatusHistoryResponse {
    }

    public static LimitedReservationStatusHistoryResponse of(LimitedReservationStatusHistory history) {
        return LimitedReservationStatusHistoryResponse.builder()
                .historyId(history.getId())
                .reservationId(history.getReservationId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .changedAt(history.getCreatedAt())
                .build();
    }
}
