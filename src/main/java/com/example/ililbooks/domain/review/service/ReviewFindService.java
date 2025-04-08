package com.example.ililbooks.domain.review.service;

import com.example.ililbooks.domain.review.dto.response.ReviewResponse;
import com.example.ililbooks.domain.review.dto.response.ReviewWithImagesResponse;
import com.example.ililbooks.domain.review.entity.Review;
import com.example.ililbooks.domain.review.repository.ReviewRepository;
import com.example.ililbooks.global.image.dto.response.ImageResponse;
import com.example.ililbooks.global.image.entity.ReviewImage;
import com.example.ililbooks.global.image.repository.ImageReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.ililbooks.global.image.dto.response.ImageResponse.ofReviewImageList;

@Service
@RequiredArgsConstructor
public class ReviewFindService {
    private final ReviewRepository reviewRepository;
    private final ImageReviewRepository imageReviewRepository;

    public Page<ReviewWithImagesResponse> getReviews(Long bookId, int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<Review> findReviews = reviewRepository.findAllByBookId(bookId, pageable);

         return findReviews
                .map(review ->
                {
                    List<ReviewImage> findReviewImage = imageReviewRepository.findAllByReviewId(review.getId());//List<ReviewImage> 반환
                    List<ImageResponse> imageResponses = ofReviewImageList(findReviewImage);// dto로 감싸기
                    return ReviewWithImagesResponse.of(review, imageResponses);
                });
    }
}
