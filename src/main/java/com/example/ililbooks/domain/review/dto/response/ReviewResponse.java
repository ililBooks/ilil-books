package com.example.ililbooks.domain.review.dto.response;

import com.example.ililbooks.domain.review.entity.Review;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class ReviewResponse {
    private Long id;

    private Long userId;

    private Long bookId;

    private int rating;

    private String comment;

    public static ReviewResponse of(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .bookId(review.getBook().getId())
                .rating(review.getRating())
                .comment(review.getComments())
                .build();
    }

    public static List<ReviewResponse> ofList(Page<Review> reviews) {
        return reviews.stream().map(ReviewResponse::of).toList();
    }
}
