package com.example.ililbooks.global.image.repository;

import com.example.ililbooks.global.image.entity.BookImage;
import com.example.ililbooks.global.image.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImageReviewRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findAllByReviewId(Long id);

    Optional<ReviewImage> findReviewImageById(Long imageId);
}
