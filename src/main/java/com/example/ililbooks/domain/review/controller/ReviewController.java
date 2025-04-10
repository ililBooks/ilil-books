package com.example.ililbooks.domain.review.controller;

import com.example.ililbooks.domain.review.dto.request.ReviewUpdateRequest;
import com.example.ililbooks.domain.review.dto.request.ReviewCreateRequest;
import com.example.ililbooks.domain.review.dto.response.ReviewResponse;
import com.example.ililbooks.domain.review.service.ReviewDeleteService;
import com.example.ililbooks.domain.review.service.ReviewService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import com.example.ililbooks.global.image.dto.request.ImageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.ADMIN;
import static com.example.ililbooks.domain.user.enums.UserRole.Authority.USER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
@Tag(name = "Review", description = "리뷰 관련 API")
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewDeleteService reviewDeleteService;

    /**
     * 리뷰 등록 API
     */
    @Operation(summary = "리뷰 등록", description = "사용자가 도서에 대해 리뷰를 작성하는 API입니다.")
    @Secured(USER)
    @PostMapping
    public Response<ReviewResponse> createReview(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ReviewCreateRequest reviewCreateRequest
    ) {
        return Response.of(reviewService.createReview(authUser, reviewCreateRequest));
    }

    /**
     * 리뷰 이미지 업로드 API
     */
    @Operation(summary = "리뷰 이미지 업로드", description = "S3에 올라간 리뷰에 대한 이미지를 업로드하는 API입니다.")
    @Secured(USER)
    @PostMapping("/{reviewId}/image")
    public Response<Void> uploadReviewImage(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long reviewId,
            @ModelAttribute ImageRequest imageRequest
    ) {
        reviewService.uploadReviewImage(authUser, reviewId, imageRequest);
        return Response.empty();
    }

    /**
     * 리뷰 수정 API
     */
    @Operation(summary = "리뷰 수정", description = "기존 리뷰 내용을 수정하는 API입니다.")
    @Secured(USER)
    @PatchMapping("/{reviewId}")
    public Response<Void> updateReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ReviewUpdateRequest reviewUpdateRequest
    ) {
        reviewService.updateReview(reviewId, authUser, reviewUpdateRequest);
        return Response.empty();
    }

    /**
     * 리뷰 삭제 API
     */
    @Operation(summary = "리뷰 삭제", description = "리뷰 ID를 이용하여 리뷰를 삭제하는 API입니다.")
    @Secured({USER, ADMIN})
    @DeleteMapping("/{reviewId}")
    public Response<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        reviewDeleteService.deleteReview(reviewId, authUser);
        return Response.empty();
    }

}
