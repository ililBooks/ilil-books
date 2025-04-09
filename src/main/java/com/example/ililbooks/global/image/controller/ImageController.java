package com.example.ililbooks.global.image.controller;

import com.example.ililbooks.global.dto.response.Response;
import com.example.ililbooks.global.image.dto.response.ImageResponse;
import com.example.ililbooks.global.image.service.S3ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ImageController {
    private final S3ImageService s3ImageService;

    /**
     * 이미지 업로드 API
     */
    @PostMapping("/images")
    public Response<ImageResponse> uploadImage(
            @RequestPart ("image") MultipartFile imageFile
    ) {
        return Response.of(s3ImageService.uploadImage(imageFile));
    }
}
