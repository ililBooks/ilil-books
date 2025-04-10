package com.example.ililbooks.domain.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.example.ililbooks.global.dto.ValidationMessage.NOT_NULL_BOOK_ID;
import static com.example.ililbooks.global.dto.ValidationMessage.NOT_NULL_RATING;

@Getter
@AllArgsConstructor
@Schema(description = "리뷰 등록 요청 DTO")
public class ReviewCreateRequest {

    @Schema(example = "3")
    @NotNull(message = NOT_NULL_BOOK_ID)
    private Long bookId;

    @Schema(example = "2")
    @NotNull(message = NOT_NULL_RATING)
    @Min(value = 0)
    @Max(value = 5)
    private int rating;

    @Schema(example = "리뷰1")
    private String comments;
}
