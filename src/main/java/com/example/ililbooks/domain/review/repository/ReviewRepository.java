package com.example.ililbooks.domain.review.repository;

import com.example.ililbooks.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findAllByBookId(Long bookId, Pageable pageable);

    boolean existsByBookIdAndUserId(Long bookId, Long userId);
}