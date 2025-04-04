package com.example.ililbooks.domain.limitedevent.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LimitedEventUpdateRequest {

    private String title;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String contents;

    private Integer bookQuantity;
}
