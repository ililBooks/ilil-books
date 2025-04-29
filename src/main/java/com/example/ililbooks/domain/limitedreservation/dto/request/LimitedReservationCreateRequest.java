package com.example.ililbooks.domain.limitedreservation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import static com.example.ililbooks.global.dto.ValidationMessage.NOT_NULL_EVENT_ID;

public record LimitedReservationCreateRequest(

        @NotNull(message = NOT_NULL_EVENT_ID)
        Long limitedEventId
) {

        @Builder
        public LimitedReservationCreateRequest(Long limitedEventId) {
                this.limitedEventId  = limitedEventId;
        }
}