package com.example.ililbooks.domain.review.repository;

import com.example.ililbooks.domain.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImageReviewRepository extends JpaRepository<ReviewImage, Long> {

    Optional<ReviewImage> findReviewImageById(Long imageId);

    int countByReviewId(Long reviewId);
    
    void deleteAllByReviewId(Long reviewId);

    @Query("SELECT r.fileName FROM ReviewImage r WHERE r.review.id = :reviewId")
    List<String> findFileNameByReviewId(@Param("reviewId") Long reviewId);

    void deleteByFileName(String fileName);

    List<ReviewImage> findAllReviewImageById(Long reviewId);

    boolean existsByReviewIdAndPositionIndex(Long reviewId, int postionIndex);

    @Query(value = "SELECT * FROM ililbooks.review_images WHERE review_id = :reviewId ORDER BY position_index  LIMIT 1", nativeQuery = true)
    Optional<ReviewImage> findFirstByReviewId(@Param("reviewId") Long reviewId);

    @Query("SELECT r.users.id FROM ReviewImage ri " +
            "JOIN ri.review r " +
            "WHERE ri.id = :id")
    Long findUserIdByReviewImageId(@Param("id") Long reviewImageId);


}
