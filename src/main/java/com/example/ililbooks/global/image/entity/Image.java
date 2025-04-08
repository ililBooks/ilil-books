package com.example.ililbooks.global.image.entity;

import lombok.Getter;

@Getter
public class Image {
    public static String extractFileName(String imageUrl) {
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }

    public static String extractExtension(String fileName) {
        String extension = "";
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
            extension = fileName.substring(dotIndex + 1).toLowerCase(); // 소문자로
        }

        return extension;
    }
}
