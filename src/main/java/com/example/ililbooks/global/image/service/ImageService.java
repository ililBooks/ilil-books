package com.example.ililbooks.global.image.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    String uploadImage(MultipartFile image);

    void deleteImage(String imageUrl);

}

