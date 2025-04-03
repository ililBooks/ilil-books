package com.example.ililbooks.domain.limitedevent.dto.response;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedevent.enums.LimitedEventStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LimitedEventResponse {

    private final Long limitedEventId;
    private final Long bookId;
    private final String title;
    private final LimitedEventStatus status;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String contents;
    private final int bookQuantity;

    public static LimitedEventResponse from(LimitedEvent event) {
        return new LimitedEventResponse(
                event.getLimitedEventId(),
                event.getBookId(),
                event.getTitle(),
                event.getStatus(),
                event.getStartTime(),
                event.getEndTime(),
                event.getContents(),
                event.getBookQuantity()
        );
    }
}
