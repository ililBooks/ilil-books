package com.example.ililbooks.domain.limitedevent.dto.response;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedevent.enums.LimitedEventStatus;
import lombok.Builder;

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

) {
    @Builder
    public LimitedEventResponse {}

    public static LimitedEventResponse of(LimitedEvent event) {
        return LimitedEventResponse.builder()
                .limitedEventId(event.getId())
                .bookId(event.getBook().getId())
                .title(event.getTitle())
                .status(event.getStatus())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .contents(event.getContents())
                .bookQuantity(event.getBookQuantity())
                .build();
    }
}