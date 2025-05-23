package com.example.ililbooks.global.image.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import static com.example.ililbooks.global.dto.ValidationMessage.*;

public record ImageRequest(

        @Schema(example = "https://ililbooks-bucket.s3.amazonaws.com/reviews/image1.png")
        @NotBlank(message = NOT_NULL_IMAGE_URL)
        String imageUrl,

        @Schema(example = "image1.png")
        @NotBlank(message = NOT_NULL_FILENAME)
        String fileName,

        @Schema(example = "png")
        @NotBlank(message = NOT_NULL_EXTENSION)
        String extension,

        @Schema(example = "3")
        @NotNull(message = NOT_NULL_POSITION_INDEX)
        @Min(value = 1, message = INVALID_POSITION_INDEX)
        @Max(value = 5, message = INVALID_POSITION_INDEX)
        int positionIndex
) {}
