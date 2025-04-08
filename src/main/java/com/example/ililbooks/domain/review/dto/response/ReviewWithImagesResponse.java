package com.example.ililbooks.domain.review.dto.response;

import com.example.ililbooks.domain.review.entity.Review;
import com.example.ililbooks.global.image.dto.response.ImageResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReviewWithImagesResponse {

    private final Long id;

    private final Long userId;

    private final Long bookId;

    private final int rating;

    private final String comment;

    private final List<ImageResponse> images;

    public static ReviewWithImagesResponse of(Review review, List<ImageResponse> images) {
        return ReviewWithImagesResponse.builder()
                .id(review.getId())
                .userId(review.getUsers().getId())
                .bookId(review.getBook().getId())
                .rating(review.getRating())
                .comment(review.getComments())
                .images(images)
                .build();
    }
}
