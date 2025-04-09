package com.example.ililbooks.domain.book.dto.response;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.enums.LimitedType;
import com.example.ililbooks.domain.book.enums.SaleStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class BookListResponse {
    private final Long id;

    private final Long userId;

    private final String title;

    private final String author;

    private final BigDecimal price;

    private final String category;

    private final int stock;

    private final String publisher;

    private final String imageUrl;

    private final String saleStatus;

    private final String limitedType;

    private final LocalDateTime createdAt;

    private final LocalDateTime modifiedAt;

    @Builder
    private BookListResponse(Long id, Long userId, String title, String author, BigDecimal price, String category, int stock, String publisher, String imageUrl, SaleStatus saleStatus, LimitedType limitedType, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.author = author;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.publisher = publisher;
        this.imageUrl = imageUrl;
        this.saleStatus = saleStatus.name();
        this.limitedType = limitedType.name();
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }
    public static BookListResponse of(Book book) {
        return BookListResponse.builder()
                .id(book.getId())
                .userId(book.getUsers().getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .price(book.getPrice())
                .category(book.getCategory())
                .stock(book.getStock())
                .publisher(book.getPublisher())
                .saleStatus(book.getSaleStatus())
                .limitedType(book.getLimitedType())
                .createdAt(book.getCreatedAt())
                .modifiedAt(book.getModifiedAt())
                .build();
    }

    public static BookListResponse of(Book book, String imageUrl) {
        return BookListResponse.builder()
                .id(book.getId())
                .userId(book.getUsers().getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .price(book.getPrice())
                .category(book.getCategory())
                .stock(book.getStock())
                .publisher(book.getPublisher())
                .imageUrl(imageUrl)
                .saleStatus(book.getSaleStatus())
                .limitedType(book.getLimitedType())
                .createdAt(book.getCreatedAt())
                .modifiedAt(book.getModifiedAt())
                .build();
    }
}
