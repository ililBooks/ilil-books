package com.example.ililbooks.domain.book.dto.response;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.review.dto.response.ReviewCreateResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public record BookResponse(
        Long id,
        Long userId,
        String title,
        String author,
        BigDecimal price,
        String category,
        int stock,
        String publisher,
        Page<ReviewCreateResponse> reviews,
        String imageUrl,
        String saleStatus,
        String limitedType
) {
    public static BookResponse of(Book book) {
        return new BookResponse(
                book.getId(),
                book.getUsers().getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPrice(),
                book.getCategory(),
                book.getStock(),
                book.getPublisher(),
                null,
                null,
                book.getSaleStatus().name(),
                book.getLimitedType().name()
        );
    }

    public static BookResponse of(Book book, String imageUrl) {
        return new BookResponse(
                book.getId(),
                book.getUsers().getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPrice(),
                book.getCategory(),
                book.getStock(),
                book.getPublisher(),
                null,
                imageUrl,
                book.getSaleStatus().name(),
                book.getLimitedType().name()
        );
    }
}
