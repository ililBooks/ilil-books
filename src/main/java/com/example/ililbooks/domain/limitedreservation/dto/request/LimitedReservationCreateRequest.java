package com.example.ililbooks.domain.limitedreservation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.example.ililbooks.global.dto.ValidationMessage.NOT_NULL_EVENT_ID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LimitedReservationCreateRequest {

    @NotNull(message = NOT_NULL_EVENT_ID)
    private Long limitedEventId;
}
