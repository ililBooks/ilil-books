package com.example.ililbooks.domain.review.service;

import com.example.ililbooks.domain.review.dto.response.ReviewResponse;
import com.example.ililbooks.domain.review.entity.Review;
import com.example.ililbooks.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewFindService {
    private final ReviewRepository reviewRepository;

    public Page<ReviewResponse> getReviews(Long bookId, int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<Review> findReviews = reviewRepository.findAllByBookId(bookId, pageable);
        return ReviewResponse.ofList(findReviews);
    }
}
