package com.example.ililbooks.global.image.service;

import com.example.ililbooks.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.util.UUID;

import static com.example.ililbooks.global.exception.ErrorMessage.*;
import static com.example.ililbooks.global.image.enums.ImageAction.REMOVE;
import static com.example.ililbooks.global.image.enums.ImageAction.ADD;


@Service
@RequiredArgsConstructor
public class S3ImageService implements ImageService{

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region}")
    private String region;

    @Override
    public String processImage(String actionType, MultipartFile image) {

        if (image.isEmpty()) {
            throw new IllegalArgumentException(NOT_FOUND_IMAGE.getMessage());
        }

        // actionType이 삭제인 경우
        if (REMOVE.name().equals(actionType)) {
            //todo: 삭제 로직 추가 예정
            return null;
        }

        if (ADD.name().equals(actionType)) {
            //고유의 UUID 생성
            String imageName = UUID.randomUUID() + "_" + image.getOriginalFilename();

            try {
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(imageName)
                        .contentType(image.getContentType())
                        .contentLength(image.getSize())
                        .build();

                //S3 업로드
                PutObjectResponse response = s3Client.putObject(
                        putObjectRequest,
                        RequestBody.fromBytes(image.getBytes())
                );

                if (response.sdkHttpResponse().isSuccessful()) {
                    return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, imageName);
                } else {
                    throw new RuntimeException(FAILED_UPLOAD_IMAGE.getMessage());
                }

            } catch (IOException e) {
                throw new RuntimeException(FAILED_UPLOAD_IMAGE.getMessage(), e);
            }
        }

        throw new BadRequestException(UNSUPPORTED_IMAGE_PROCESSING_TYPE.getMessage() + actionType);
    }
}
