package com.example.ililbooks.domain.bestseller.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BestSellerChartResponse(
        int rank,
        String title,
        String author,
        String category,
        String publisher,
        BigDecimal price,
        String imageUrl,
        String saleStatus,
        String limitedType
) {
}
