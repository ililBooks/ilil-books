package com.example.ililbooks.domain.review.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private int positionIndex;

    @Builder
    private ReviewImage(Review review, String imageUrl, String fileName, String extension, int positionIndex) {
        this.review = review;
        this.imageUrl = imageUrl;
        this.fileName = fileName;
        this.extension = extension;
        this.positionIndex = positionIndex;
    }

    public static ReviewImage of(Review review, String imageUrl, String fileName, String extension, int positionIndex) {
        return ReviewImage.builder()
                .review(review)
                .imageUrl(imageUrl)
                .fileName(fileName)
                .extension(extension)
                .positionIndex(positionIndex)
                .build();
    }
}
