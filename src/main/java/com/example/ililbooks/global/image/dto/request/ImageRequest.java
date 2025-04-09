package com.example.ililbooks.global.image.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.example.ililbooks.global.dto.ValidationMessage.*;

@Getter
@AllArgsConstructor
public class ImageRequest {
    @NotBlank(message = NOT_NULL_IMAGE_URL)
    private String imageUrl;

    @NotBlank(message = NOT_NULL_FILENAME)
    private String fileName;

    @NotBlank(message = NOT_NULL_EXTENSION)
    private String extension;
}
