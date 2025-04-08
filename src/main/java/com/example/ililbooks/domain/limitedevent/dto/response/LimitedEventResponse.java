package com.example.ililbooks.domain.limitedevent.dto.response;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedevent.enums.LimitedEventStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LimitedEventResponse {

    private final Long limitedEventId;
    private final Long bookId;
    private final String title;
    private final LimitedEventStatus status;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String contents;
    private final int bookQuantity;

    public static LimitedEventResponse from(LimitedEvent limitedEvent) {
        return LimitedEventResponse.builder()
                .limitedEventId(limitedEvent.getId())
                .bookId(limitedEvent.getBook().getId())
                .title(limitedEvent.getTitle())
                .status(limitedEvent.getStatus())
                .startTime(limitedEvent.getStartTime())
                .endTime(limitedEvent.getEndTime())
                .contents(limitedEvent.getContents())
                .bookQuantity(limitedEvent.getBookQuantity())
                .build();
    }
}
