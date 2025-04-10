package com.example.ililbooks.domain.review.service;

import com.example.ililbooks.domain.review.dto.response.ReviewWithImagesResponse;
import com.example.ililbooks.domain.review.entity.Review;
import com.example.ililbooks.domain.review.entity.ReviewImage;
import com.example.ililbooks.domain.review.repository.ImageReviewRepository;
import com.example.ililbooks.domain.review.repository.ReviewRepository;
import com.example.ililbooks.global.image.dto.response.ImageListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.ililbooks.global.image.dto.response.ImageListResponse.ofReviewImageList;

@Service
@RequiredArgsConstructor
public class ReviewFindService {
    private final ReviewRepository reviewRepository;
    private final ImageReviewRepository imageReviewRepository;

    public Page<ReviewWithImagesResponse> getReviews(Long bookId, int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<Review> reviews = reviewRepository.findAllByBookId(bookId, pageable);

         return reviews
                .map(review ->
                {
                    //리뷰에 저장되어 있는 이미지 리스트 출력
                    List<ReviewImage> reviewImage = imageReviewRepository.findAllByReviewId(review.getId());

                    //ImageResponse로 감싸서 반환
                    List<ImageListResponse> imageResponses = ofReviewImageList(reviewImage);
                    return ReviewWithImagesResponse.of(review, imageResponses);
                });
    }
}
