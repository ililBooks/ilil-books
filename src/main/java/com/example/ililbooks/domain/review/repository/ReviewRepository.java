package com.example.ililbooks.domain.review.repository;

import com.example.ililbooks.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findAllByBookId(Long bookId);

    boolean existsByBookIdAndUserId(Long bookId, Long userId);
}