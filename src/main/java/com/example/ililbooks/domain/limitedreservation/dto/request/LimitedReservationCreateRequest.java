package com.example.ililbooks.domain.limitedreservation.dto.request;

import jakarta.validation.constraints.NotNull;

import static com.example.ililbooks.global.dto.ValidationMessage.NOT_NULL_EVENT_ID;

public record LimitedReservationCreateRequest(

        @NotNull(message = NOT_NULL_EVENT_ID)
        Long limitedEventId
) {}