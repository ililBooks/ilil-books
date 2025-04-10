package com.example.ililbooks.domain.limitedevent.dto.response;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedevent.enums.LimitedEventStatus;

import java.time.Instant;

public record LimitedEventResponse(

        Long limitedEventId,
        Long bookId,
        String title,
        LimitedEventStatus status,
        Instant startTime,
        Instant endTime,
        String contents,
        int bookQuantity

) {}