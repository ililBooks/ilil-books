package com.example.ililbooks.global.image.dto.response;

import com.example.ililbooks.domain.book.entity.BookImage;
import com.example.ililbooks.domain.review.entity.ReviewImage;

import java.util.List;

public record ImageListResponse (
        String imageUrl
)
{
    public static ImageListResponse of(String imageUrl) {
        return new ImageListResponse(imageUrl);
    }

    public static List<ImageListResponse> ofBookImageList(List<BookImage> images) {
        return images.stream()
                .map(image -> new ImageListResponse(image.getImageUrl()))
                .toList();
    }

    public static List<ImageListResponse> ofReviewImageList(List<ReviewImage> images) {
        return images.stream()
                .map(image -> new ImageListResponse(image.getImageUrl()))
                .toList();
    }
}
