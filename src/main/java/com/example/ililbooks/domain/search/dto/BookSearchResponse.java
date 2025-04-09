package com.example.ililbooks.domain.search.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class BookSearchResponse {

    private final String title;
    private final String author;
    private final String publisher;
    private final BigDecimal price;
    private final String category;
    private final String saleStatus;
    private final String limitedType;

    @Builder
    private BookSearchResponse(String title, String author, String publisher, BigDecimal price, String category, String saleStatus, String limitedType) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.price = price;
        this.category = category;
        this.saleStatus = saleStatus;
        this.limitedType = limitedType;
    }

}
