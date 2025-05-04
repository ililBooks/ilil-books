package com.example.ililbooks.domain.book.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

import static com.example.ililbooks.global.dto.ValidationMessage.*;

@Schema(description = "책 생성 요청 DTO")
public record BookCreateRequest(

        @Schema(example = "자바 ORM 표준 JPA 프로그래밍")
        @NotBlank(message = NOT_NULL_TITLE)
        String title,

        @Schema(example = "김영한")
        @NotBlank(message = NOT_NULL_AUTHOR)
        String author,

        @Schema(example = "36000")
        @NotNull(message = NOT_NULL_PRICE)
        BigDecimal price,

        @Schema(example = "프로그래밍")
        @NotBlank(message = NOT_NULL_CATEGORY)
        String category,

        @Schema(example = "50", minimum = "0")
        @NotNull(message = NOT_NULL_STOCK)
        @Min(value = 0, message = INVALID_STOCK)
        int stock,

        @Schema(example = "9788960773330")
        @NotBlank(message = NOT_NULL_ISBN)
        String isbn,

        @Schema(example = "문예한국사")
        @NotBlank(message = NOT_NULL_PUBLISHER)
        String publisher,

        @Schema(example = "REGULAR")
        @NotBlank(message = NOT_NULL_LIMITED_TYPE)
        String limitedType
) {}
