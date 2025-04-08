package com.example.ililbooks.domain.review.repository;

import com.example.ililbooks.domain.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImageReviewRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findAllByReviewId(Long id);

    Optional<ReviewImage> findReviewImageById(Long imageId);

    int countByReviewId(Long reviewId);
}
