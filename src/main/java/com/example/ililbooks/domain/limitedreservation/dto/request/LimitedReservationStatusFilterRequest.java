package com.example.ililbooks.domain.limitedreservation.dto.request;

import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record LimitedReservationStatusFilterRequest(

        @NotNull
        Long eventId,

        @NotEmpty
        List<LimitedReservationStatus> statuses

) {}