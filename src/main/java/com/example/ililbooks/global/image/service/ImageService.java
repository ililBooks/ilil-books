package com.example.ililbooks.global.image.service;

import com.example.ililbooks.global.dto.AuthUser;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    String processImage(String actions, MultipartFile image);
}

