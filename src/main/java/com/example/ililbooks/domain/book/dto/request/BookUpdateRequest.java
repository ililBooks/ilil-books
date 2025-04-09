package com.example.ililbooks.domain.book.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

import static com.example.ililbooks.global.dto.ValidationMessage.*;
import static com.example.ililbooks.global.dto.ValidationMessage.INVALID_STOCK;

@Getter
@AllArgsConstructor
public class BookUpdateRequest {
    @NotBlank(message = NOT_NULL_TITLE)
    private String title;

    @NotBlank(message = NOT_NULL_AUTHOR)
    private String author;

    @NotNull(message = NOT_NULL_PRICE)
    private BigDecimal price;

    @NotNull(message = NOT_NULL_CATEGORY)
    private String category;

    @NotNull(message = NOT_NULL_STOCK)
    @Min(value = 0, message = INVALID_STOCK)
    private int stock;

    @NotBlank(message = NOT_NULL_SALE_STATUS)
    private String saleStatus;

    @NotBlank(message = NOT_NULL_LIMITED_TYPE)
    private String limitedType;
}
