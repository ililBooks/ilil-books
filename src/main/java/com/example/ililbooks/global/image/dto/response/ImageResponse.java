package com.example.ililbooks.global.image.dto.response;

public record ImageResponse(
        String imageUrl,
        String fileName,
        String extension
) {

    public static ImageResponse of(String imageUrl, String fileName, String extension) {
        return new ImageResponse(imageUrl, fileName, extension);
    }
}
