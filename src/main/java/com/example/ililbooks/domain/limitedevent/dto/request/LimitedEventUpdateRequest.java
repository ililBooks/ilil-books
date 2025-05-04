package com.example.ililbooks.domain.limitedevent.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.Instant;

import static com.example.ililbooks.global.dto.ValidationMessage.*;

public record LimitedEventUpdateRequest (

        @NotBlank(message = NOT_NULL_EVENT_TITLE)
        String title,

        @NotNull(message = NOT_NULL_START_DATE)
        Instant startTime,

        @NotNull(message = NOT_NULL_END_DATE)
        @Future(message = FUTURE_EVENT_END_DATE)
        Instant endTime,

        @NotBlank(message = NOT_BLANK_EVENT_DESCRIPTION)
        String contents,

        @NotNull(message = INVALID_EVENT_QUANTITY)
        @Min(value = 1, message = INVALID_EVENT_QUANTITY)
        Integer bookQuantity
) {

        @Builder
        public LimitedEventUpdateRequest(String title, Instant startTime, Instant endTime, String contents, Integer bookQuantity) {
                this.title = title;
                this.startTime = startTime;
                this.endTime = endTime;
                this.contents = contents;
                this.bookQuantity  = bookQuantity;
        }
}