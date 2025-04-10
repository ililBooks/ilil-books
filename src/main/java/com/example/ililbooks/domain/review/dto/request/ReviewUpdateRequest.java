package com.example.ililbooks.domain.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import static com.example.ililbooks.global.dto.ValidationMessage.NOT_NULL_RATING;

@Schema(description = "리뷰 수정 요청 DTO")
public record ReviewUpdateRequest(

        @Schema(example = "5")
        @NotNull(message = NOT_NULL_RATING)
        @Min(value = 0)
        @Max(value = 5)
        int rating,

        @Schema(example = "리뷰 수정")
        String comments
) {}
