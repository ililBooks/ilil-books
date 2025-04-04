package com.example.ililbooks.domain.limitedevent.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

import static com.example.ililbooks.global.dto.ValidationMessage.*;

@Getter
@AllArgsConstructor
public class LimitedEventCreateRequest {

    @NotNull(message = NOT_NULL_BOOK_ID)
    private Long bookId;

    @NotBlank(message = NOT_NULL_EVENT_TITLE)
    private String title;

    @NotNull(message = NOT_NULL_START_DATE)
    private LocalDateTime startTime;

    @NotNull(message = NOT_NULL_END_DATE)
    @Future(message = FUTURE_EVENT_END_DATE)
    private LocalDateTime endTime;

    @NotBlank(message = NOT_BLANK_EVENT_DESCRIPTION)
    private String contents;

    @Min(value = 1, message = INVALID_EVENT_QUANTITY)
    private Integer bookQuantity;
}
