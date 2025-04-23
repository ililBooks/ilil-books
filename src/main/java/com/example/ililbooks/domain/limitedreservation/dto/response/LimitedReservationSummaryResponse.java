package com.example.ililbooks.domain.limitedreservation.dto.response;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import lombok.Builder;

public record LimitedReservationSummaryResponse(
        Long limitedEventId,
        String eventTitle,
        Long successCount,
        Long waitingCount,
        Long canceledCount
) {

    @Builder
    public LimitedReservationSummaryResponse {
    }

    public static LimitedReservationSummaryResponse of(LimitedEvent limitedEvent, Long successCount, Long waitingCount, Long canceledCount) {
        return LimitedReservationSummaryResponse.builder()
                .limitedEventId(limitedEvent.getId())
                .eventTitle(limitedEvent.getTitle())
                .successCount(successCount)
                .waitingCount(waitingCount)
                .canceledCount(canceledCount)
                .build();
    }
}
