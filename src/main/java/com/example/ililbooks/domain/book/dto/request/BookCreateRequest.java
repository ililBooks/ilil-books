package com.example.ililbooks.domain.book.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

import static com.example.ililbooks.global.dto.ValidationMessage.*;

@Getter
@AllArgsConstructor
@Schema(description = "책 생성 요청 DTO")
public class BookCreateRequest {

    @Schema(example = "자바 ORM 표준 JPA 프로그래밍")
    @NotBlank(message = NOT_NULL_TITLE)
    private String title;

    @Schema(example = "김영한")
    @NotBlank(message = NOT_NULL_AUTHOR)
    private String author;

    @Schema(example = "36000")
    @NotNull(message = NOT_NULL_PRICE)
    private BigDecimal price;

    @Schema(example = "프로그래밍")
    @NotBlank(message = NOT_NULL_CATEGORY)
    private String category;

    @Schema(example = "50", minimum = "0")
    @NotNull(message = NOT_NULL_STOCK)
    @Min(value = 0, message = INVALID_STOCK)
    private int stock;

    @Schema(example = "9788960773330")
    @NotBlank(message = NOT_NULL_ISBN)
    private String isbn;

    @Schema(example = "문예한국사")
    @NotBlank(message = NOT_NULL_PUBLISHER)
    private String publisher;
}
