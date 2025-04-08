package com.example.ililbooks.domain.review.dto.response;

import com.example.ililbooks.domain.review.entity.Review;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewResponse {
    private final Long id;

    private final Long userId;

    private final Long bookId;

    private final int rating;

    private final String comment;

    @Builder
    public ReviewResponse(Long id, Long userId, Long bookId, int rating, String comment) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.rating = rating;
        this.comment = comment;
    }

    public static ReviewResponse of(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUsers().getId())
                .bookId(review.getBook().getId())
                .rating(review.getRating())
                .comment(review.getComments())
                .build();
    }

}
