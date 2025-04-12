package com.example.ililbooks.domain.search.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

public record BookSearchResponse(
        String title,
        String author,
        String publisher,
        BigDecimal price,
        String category,
        String saleStatus,
        String limitedType
) {
    @Builder
    public BookSearchResponse {
    }

}
