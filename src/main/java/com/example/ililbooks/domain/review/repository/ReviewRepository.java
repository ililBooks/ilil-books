package com.example.ililbooks.domain.review.repository;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findAllByBookId(Long bookId, Pageable pageable);

    List<Review> findReviewsByBookId(Long bookId);

    boolean existsByBookIdAndUsersId(Long bookId, Long userId);

    void deleteAllByBookId(Long bookId);

    Long book(Book book);

    Optional<Review> findReviewById(Long reviewId);
}