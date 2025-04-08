package com.example.ililbooks.global.image.dto.response;

import com.example.ililbooks.domain.book.entity.BookImage;
import com.example.ililbooks.domain.review.entity.ReviewImage;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ImageResponse {
    private final String imageUrl;

    @Builder
    private ImageResponse (String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public static ImageResponse of(String imageUrl) {
        return ImageResponse.builder()
                .imageUrl(imageUrl)
                .build();
    }

    public static List<ImageResponse> ofBookImageList(List<BookImage> images) {
        return images.stream()
                .map(image ->
                        ImageResponse.of(image.getImageUrl())
                )
                .toList();
    }

    public static List<ImageResponse> ofReviewImageList(List<ReviewImage> images) {
        return images.stream()
                .map(image ->
                        ImageResponse.of(image.getImageUrl())
                )
                .toList();
    }

}
