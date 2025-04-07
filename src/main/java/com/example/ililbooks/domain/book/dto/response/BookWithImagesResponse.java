package com.example.ililbooks.domain.book.dto.response;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.enums.LimitedType;
import com.example.ililbooks.domain.book.enums.SaleStatus;
import com.example.ililbooks.domain.review.dto.response.ReviewResponse;
import com.example.ililbooks.global.image.dto.response.ImageResponse;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class BookWithImagesResponse {
    private final Long id;

    private final Long userId;

    private final String title;

    private final String author;

    private final Long price;

    private final String category;

    private final int stock;

    private final Page<ReviewResponse> reviews;

    private final List<ImageResponse> imageUrl;

    private final String saleStatus;

    private final String limitedType;

    private final LocalDateTime createdAt;

    private final LocalDateTime modifiedAt;

    @Builder
    private BookWithImagesResponse(Long id, Long userId, String title, String author, Long price, String category, int stock, Page<ReviewResponse> reviews, List<ImageResponse> imageUrl, SaleStatus saleStatus, LimitedType limitedType, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.author = author;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.reviews = reviews;
        this.imageUrl = imageUrl;
        this.saleStatus = saleStatus.name();
        this.limitedType = limitedType.name();
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public static BookWithImagesResponse of(Book book, Page<ReviewResponse> reviews, List<ImageResponse> bookImages) {
        return BookWithImagesResponse.builder()
                .id(book.getId())
                .userId(book.getUsers().getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .price(book.getPrice())
                .category(book.getCategory())
                .stock(book.getStock())
                .reviews(reviews)
                .imageUrl(bookImages)
                .saleStatus(book.getSaleStatus())
                .limitedType(book.getLimitedType())
                .createdAt(book.getCreatedAt())
                .modifiedAt(book.getModifiedAt())
                .build();
    }
}
