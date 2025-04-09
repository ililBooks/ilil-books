package com.example.ililbooks.domain.limitedevent.dto.request;

import java.time.Instant;

public record LimitedEventUpdateRequest (

        String title,
        Instant startTime,
        Instant endTime,
        String contents,
        Integer bookQuantity
) {}