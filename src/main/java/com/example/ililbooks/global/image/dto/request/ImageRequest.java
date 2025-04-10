package com.example.ililbooks.global.image.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.example.ililbooks.global.dto.ValidationMessage.*;

@Getter
@AllArgsConstructor
public class ImageRequest {

    @Schema(example = "https://ililbooks-bucket.s3.amazonaws.com/reviews/image1.png")
    @NotBlank(message = NOT_NULL_IMAGE_URL)
    private String imageUrl;

    @Schema(example = "image1.png")
    @NotBlank(message = NOT_NULL_FILENAME)
    private String fileName;

    @Schema(example = ".png")
    @NotBlank(message = NOT_NULL_EXTENSION)
    private String extension;
}
