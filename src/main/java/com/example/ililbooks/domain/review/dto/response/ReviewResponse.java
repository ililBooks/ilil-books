package com.example.ililbooks.domain.review.dto.response;

import com.example.ililbooks.domain.review.entity.Review;

public record ReviewResponse(
        Long id,
        Long userId,
        Long bookId,
        int rating,
        String comment
) {
    public static ReviewResponse of(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUsers().getId(),
                review.getBook().getId(),
                review.getRating(),
                review.getComments()
        );
    }
}
