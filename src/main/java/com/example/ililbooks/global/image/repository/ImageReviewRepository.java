package com.example.ililbooks.global.image.repository;

import com.example.ililbooks.global.image.entity.BookImage;
import com.example.ililbooks.global.image.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageReviewRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findAllByReviewId(Long id);
}
