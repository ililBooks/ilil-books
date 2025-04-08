package com.example.ililbooks.global.image.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    String uploadImage(MultipartFile image);

    String deleteImage(String imageUrl);
}

