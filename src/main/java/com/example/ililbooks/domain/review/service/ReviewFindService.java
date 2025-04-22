package com.example.ililbooks.domain.review.service;

import com.example.ililbooks.domain.review.dto.response.ReviewWithImagesResponse;
import com.example.ililbooks.domain.review.entity.Review;
import com.example.ililbooks.domain.review.entity.ReviewImage;
import com.example.ililbooks.domain.review.repository.ImageReviewRepository;
import com.example.ililbooks.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewFindService {
    private final ReviewRepository reviewRepository;
    private final ImageReviewRepository imageReviewRepository;

    public Page<ReviewWithImagesResponse> getReviews(Long bookId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findAllByBookId(bookId, pageable);

         return reviews
                .map(review ->
                {
                    //리뷰에 저장되어 있는 이미지 리스트 출력
                    ReviewImage reviewImage = imageReviewRepository.findFirstByReviewId(review.getId()).orElse(null);

                    if(reviewImage == null) {
                        return ReviewWithImagesResponse.of(review);
                    }

                    return ReviewWithImagesResponse.of(review, reviewImage.getImageUrl());
                });
    }
}
