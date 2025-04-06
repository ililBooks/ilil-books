package com.example.ililbooks.domain.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.example.ililbooks.global.dto.ValidationMessage.NOT_NULL_RATING;

@Getter
@AllArgsConstructor
public class ReviewUpdateRequest {
    @NotNull(message = NOT_NULL_RATING)
    @Min(value = 0)
    @Max(value = 5)
    private int rating;

    private String comments;
}
