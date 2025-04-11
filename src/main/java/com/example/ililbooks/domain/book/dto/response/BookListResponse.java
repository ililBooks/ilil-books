package com.example.ililbooks.domain.book.dto.response;

import com.example.ililbooks.domain.book.entity.Book;

import java.math.BigDecimal;

public record BookListResponse (
        Long id,
        Long userId,
        String title,
        String author,
        BigDecimal price,
        String category,
        int stock,
        String publisher,
        String imageUrl,
        String saleStatus,
        String limitedType
) {
    public static BookListResponse of(Book book) {
        return new BookListResponse(
                book.getId(),
                book.getUsers().getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPrice(),
                book.getCategory(),
                book.getStock(),
                book.getPublisher(),
                null,
                book.getSaleStatus().name(),
                book.getLimitedType().name()
        );
    }

    public static BookListResponse of(Book book, String imageUrl) {
        return new BookListResponse(
                book.getId(),
                book.getUsers().getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPrice(),
                book.getCategory(),
                book.getStock(),
                book.getPublisher(),
                imageUrl,
                book.getSaleStatus().name(),
                book.getLimitedType().name()
        );
    }
}
