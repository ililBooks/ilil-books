package com.example.ililbooks.global.image.service;

import com.example.ililbooks.global.image.dto.response.ImageResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    ImageResponse uploadImage(MultipartFile image);

    void deleteImage(String imageUrl);

}

