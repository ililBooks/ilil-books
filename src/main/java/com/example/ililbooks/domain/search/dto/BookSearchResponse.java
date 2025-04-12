package com.example.ililbooks.domain.search.dto;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.search.entity.BookDocument;
import lombok.Builder;

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

    public static BookSearchResponse of(BookDocument bookDocument) {
        return BookSearchResponse.builder()
                .title(bookDocument.getTitle())
                .author(bookDocument.getAuthor())
                .publisher(bookDocument.getPublisher())
                .price(bookDocument.getPrice())
                .category(bookDocument.getCategory())
                .saleStatus(bookDocument.getSaleStatus())
                .limitedType(bookDocument.getLimitedType())
                .build();
    }

    public static BookSearchResponse of(Book book) {
        return BookSearchResponse.builder()
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .price(book.getPrice())
                .category(book.getCategory())
                .saleStatus(book.getSaleStatus().name())
                .limitedType(book.getLimitedType().name())
                .build();
    }
}
