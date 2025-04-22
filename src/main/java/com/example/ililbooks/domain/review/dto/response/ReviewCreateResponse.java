package com.example.ililbooks.domain.review.dto.response;

import com.example.ililbooks.domain.review.entity.Review;

public record ReviewCreateResponse(
        Long id,
        Long userId,
        Long bookId,
        int rating,
        String comment
) {
    public static ReviewCreateResponse of(Review review) {
        return new ReviewCreateResponse(
                review.getId(),
                review.getUsers().getId(),
                review.getBook().getId(),
                review.getRating(),
                review.getComments()
        );
    }
}
