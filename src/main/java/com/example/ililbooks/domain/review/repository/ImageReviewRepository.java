package com.example.ililbooks.domain.review.repository;

import com.example.ililbooks.domain.review.entity.Review;
import com.example.ililbooks.domain.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImageReviewRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findAllByReviewId(Long id);

    Optional<ReviewImage> findReviewImageById(Long imageId);

    int countByReviewId(Long reviewId);
    
    void deleteAllByReviewId(Long reviewId);

    @Query("SELECT r.fileName FROM ReviewImage r WHERE r.review.id = :reviewId")
    List<String> findFileNameByReviewId(@Param("reviewId") Long reviewId);

    void deleteByFileName(String fileName);

    List<ReviewImage> findAllReviewImageById(Long reviewId);
}
