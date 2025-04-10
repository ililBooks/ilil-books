package com.example.ililbooks.domain.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.example.ililbooks.global.dto.ValidationMessage.NOT_NULL_RATING;

@Getter
@AllArgsConstructor
@Schema(description = "리뷰 수정 요청 DTO")
public class ReviewUpdateRequest {

    @Schema(example = "5")
    @NotNull(message = NOT_NULL_RATING)
    @Min(value = 0)
    @Max(value = 5)
    private int rating;

    @Schema(example = "리뷰 수정")
    private String comments;
}
