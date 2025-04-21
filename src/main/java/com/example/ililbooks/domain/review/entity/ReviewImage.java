package com.example.ililbooks.domain.review.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//TODO 이미지가 한 리뷰에 많이 저장되는 관계면 우선순위(대표 이미지) 등 필요
@Getter
@Entity
@Table(name = "review_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    private String imageUrl;

    private String fileName;

    private String extension;

    @Builder
    private ReviewImage(Review review, String imageUrl, String fileName, String extension) {
        this.review = review;
        this.imageUrl = imageUrl;
        this.fileName = fileName;
        this.extension = extension;
    }

    public static ReviewImage of(Review review, String imageUrl, String fileName, String extension) {
        return ReviewImage.builder()
                .review(review)
                .imageUrl(imageUrl)
                .fileName(fileName)
                .extension(extension)
                .build();
    }
}
