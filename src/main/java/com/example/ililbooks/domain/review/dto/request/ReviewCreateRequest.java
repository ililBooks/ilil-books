package com.example.ililbooks.domain.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import static com.example.ililbooks.global.dto.ValidationMessage.NOT_NULL_BOOK_ID;
import static com.example.ililbooks.global.dto.ValidationMessage.NOT_NULL_RATING;

@Schema(description = "리뷰 등록 요청 DTO")
public record ReviewCreateRequest(

        @Schema(example = "3")
        @NotNull(message = NOT_NULL_BOOK_ID)
        Long bookId,

        @Schema(example = "2")
        @NotNull(message = NOT_NULL_RATING)
        @Min(value = 0)
        @Max(value = 5)
        int rating,

        @Schema(example = "리뷰1")
        String comments
) {}
