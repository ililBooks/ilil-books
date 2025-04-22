package com.example.ililbooks.domain.review.controller;

import com.example.ililbooks.domain.review.service.ReviewDeleteService;
import com.example.ililbooks.domain.review.service.ReviewService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.ADMIN;
import static com.example.ililbooks.domain.user.enums.UserRole.Authority.USER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/review-images")
@Tag(name = "Review Image", description = "리뷰 이미지 관련 API")
public class ReviewImageController {
    private final ReviewDeleteService reviewDeleteService;

    /**
     * 리뷰 이미지 삭제 API
     */
    @Operation(summary = "리뷰 이미지 삭제", description = "리뷰 이미지 ID를 이용하여 이미지를 삭제하는 API입니다.")
    @Secured({USER, ADMIN})
    @DeleteMapping("/{imageId}")
    public Response<Void> deleteReviewImage(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long imageId
    ) {
        reviewDeleteService.deleteReviewImage(authUser, imageId);
        return Response.empty();
    }
}
