package com.example.ililbooks.global.image.controller;

import com.example.ililbooks.global.dto.response.Response;
import com.example.ililbooks.global.image.dto.response.ImageResponse;
import com.example.ililbooks.global.image.service.S3ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "image", description = "이미지 업로드 관련 API")
public class ImageController {
    private final S3ImageService s3ImageService;

    /**
     * 이미지 업로드 API
     */
    @Operation(summary = "이미지 업로드", description = "Multipart 형식으로 입력받은 이미지를 처리하여 S3에 업로드하는 API 입니다.")
    @PostMapping("/images")
    public Response<ImageResponse> uploadImage(
            @RequestPart ("image") MultipartFile imageFile
    ) {
        return Response.of(s3ImageService.uploadImage(imageFile));
    }
}
