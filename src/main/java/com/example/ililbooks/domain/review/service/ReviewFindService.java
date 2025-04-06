package com.example.ililbooks.domain.review.service;

import com.example.ililbooks.domain.review.dto.response.ReviewResponse;
import com.example.ililbooks.domain.review.entity.Review;
import com.example.ililbooks.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewFindService {
    private final ReviewRepository reviewRepository;

    public List<ReviewResponse> getReviews(Long bookId) {
        List<Review> findReviews = reviewRepository.findAllByBookId(bookId);
        return ReviewResponse.ofList(findReviews);
    }
}
