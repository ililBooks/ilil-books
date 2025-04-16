package com.example.ililbooks.global.image.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import static com.example.ililbooks.global.exception.ErrorMessage.NOT_FOUND_IMAGE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class S3ImageServiceTest {
    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3ImageService s3ImageService;

    @Mock
    private MultipartFile multipartFile;

    @Test
    void 입력된_이미지가_비어있어_S3에_업로드_실패() {
        //given
        given(multipartFile.isEmpty()).willReturn(true);

        //when & then
        assertThrows(IllegalArgumentException.class,
                () -> s3ImageService.uploadImage(multipartFile),
                NOT_FOUND_IMAGE.getMessage()
        );
    }

}