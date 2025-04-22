package com.example.ililbooks.domain.review.dto.response;

import com.example.ililbooks.domain.review.entity.Review;
import com.example.ililbooks.global.image.dto.response.ImageListResponse;
import com.example.ililbooks.global.image.dto.response.ImageResponse;

import java.util.List;

public record ReviewWithImagesResponse(
        Long id,
        Long userId,
        Long bookId,
        int rating,
        String comment,
        String image
) {
    public static ReviewWithImagesResponse of(Review review, String image) {
        return new ReviewWithImagesResponse(
                review.getId(),
                review.getUsers().getId(),
                review.getBook().getId(),
                review.getRating(),
                review.getComments(),
                image
        );
    }

    public static ReviewWithImagesResponse of(Review review) {
        return new ReviewWithImagesResponse(
                review.getId(),
                review.getUsers().getId(),
                review.getBook().getId(),
                review.getRating(),
                review.getComments(),
                null
        );
    }
}
