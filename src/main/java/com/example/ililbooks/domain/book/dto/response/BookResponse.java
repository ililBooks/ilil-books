package com.example.ililbooks.domain.book.dto.response;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.enums.LimitedType;
import com.example.ililbooks.domain.book.enums.SaleStatus;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class BookResponse {
    private final Long id;

    private final Long userId;

    private final String title;

    private final String author;

    private final Long price;

    private final String category;

    private final int stock;

    //todo: 리뷰 리스트 추가

    //todo: 이미지 추가

    private final String saleStatus;

    private final String limitedType;

    private final LocalDateTime createdAt;

    private final LocalDateTime modifiedAt;

    @Builder
    private BookResponse(Long id, Long userId, String title, String author, Long price, String category, int stock, SaleStatus saleStatus, LimitedType limitedType, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.author = author;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.saleStatus = saleStatus.name();
        this.limitedType = limitedType.name();
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public static BookResponse of(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .userId(book.getUsers().getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .price(book.getPrice())
                .category(book.getCategory())
                .stock(book.getStock())
                .saleStatus(book.getSaleStatus())
                .limitedType(book.getLimitedType())
                .createdAt(book.getCreatedAt())
                .modifiedAt(book.getModifiedAt())
                .build();
    }

    public static List<BookResponse> ofList(Page<Book> books) {
        return books.stream()
                .map(BookResponse::of)
                .toList();
    }
}
