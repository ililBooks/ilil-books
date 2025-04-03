package com.example.ililbooks.domain.limitedevent.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LimitedEventUpdateRequest {

    @NotBlank
    String title;

    @NotNull
    LocalDateTime startTime;

    @NotNull
    LocalDateTime endTime;

    @NotBlank
    String contents;

    @NotNull
    Integer bookQuantity;
}
