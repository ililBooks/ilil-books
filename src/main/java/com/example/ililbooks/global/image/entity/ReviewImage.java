package com.example.ililbooks.global.image.entity;

import com.example.ililbooks.domain.review.entity.Review;
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

    @Builder
    private ReviewImage(Review review, String imageUrl, String fileName, String extension) {
        this.review = review;
        this.imageUrl = imageUrl;
        this.fileName = fileName;
        this.extension = extension;
    }

    public static ReviewImage of(Review review, String imageUrl) {

        // URL에서 파일 이름 추출
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

        // 확장자 추출
        String extension = "";
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
            extension = fileName.substring(dotIndex + 1).toLowerCase(); // 소문자로
        }

        return ReviewImage.builder()
                .review(review)
                .imageUrl(imageUrl)
                .fileName(fileName)
                .extension(extension)
                .build();
    }


}
