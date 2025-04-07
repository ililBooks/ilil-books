package com.example.ililbooks.global.image.repository;

import com.example.ililbooks.global.image.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageReviewRepository extends JpaRepository<ReviewImage, Long> {
}
