package com.example.ililbooks.domain.book.dto.response;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.review.dto.response.ReviewWithImagesResponse;
import com.example.ililbooks.global.image.dto.response.ImageListResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public record BookWithImagesResponse(
        Long id,
        Long userId,
        String title,
        String author,
        BigDecimal price,
        String category,
        int stock,
        Page<ReviewWithImagesResponse> reviews,
        List<ImageListResponse> imageUrl,
        String saleStatus,
        String limitedType
) {
    public static BookWithImagesResponse of(Book book, Page<ReviewWithImagesResponse> reviews, List<ImageListResponse> bookImages) {
        return new BookWithImagesResponse(
                book.getId(),
                book.getUsers().getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPrice(),
                book.getCategory(),
                book.getStock(),
                reviews,
                bookImages,
                book.getSaleStatus().name(),
                book.getLimitedType().name()
        );
    }
}
