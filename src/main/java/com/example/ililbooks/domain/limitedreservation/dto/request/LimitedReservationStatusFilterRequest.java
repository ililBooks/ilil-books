package com.example.ililbooks.domain.limitedreservation.dto.request;

import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

import static com.example.ililbooks.global.dto.ValidationMessage.NOT_EMPTY_RESERVATION_STATUS;
import static com.example.ililbooks.global.dto.ValidationMessage.NOT_NULL_EVENT_ID;

public record LimitedReservationStatusFilterRequest(

        @NotNull(message = NOT_NULL_EVENT_ID)
        Long eventId,

        @NotEmpty(message = NOT_EMPTY_RESERVATION_STATUS)
        List<LimitedReservationStatus> statuses,

        Long userId,
        Instant startDate,
        Instant endDate
) {

        @Builder
        public LimitedReservationStatusFilterRequest(Long eventId, List<LimitedReservationStatus> statuses, Long userId, Instant startDate, Instant endDate) {
                this.eventId = eventId;
                this.statuses = statuses;
                this.userId = userId;
                this.startDate = startDate;
                this.endDate = endDate;
        }
}