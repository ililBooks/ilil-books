package com.example.ililbooks.domain.review.controller;

import com.example.ililbooks.domain.review.dto.request.ReviewUpdateRequest;
import com.example.ililbooks.domain.review.dto.request.ReviewCreateRequest;
import com.example.ililbooks.domain.review.dto.response.ReviewResponse;
import com.example.ililbooks.domain.review.service.ReviewService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
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
public class ReviewController {
    private final ReviewService reviewService;

    /**
     * 리뷰 등록 API
     */
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
    @Secured(USER)
    @PostMapping("/{reviewId}/image")
    public Response<Void> uploadReviewImage(
            @PathVariable Long reviewId,
            @RequestParam String imageUrl
    ) {
        reviewService.uploadReviewImage(reviewId, imageUrl);
        return Response.empty();
    }

    /**
     * 리뷰 이미지 삭제 API
     */
    @Secured({USER, ADMIN})
    @DeleteMapping("/{imageId}/image")
    public Response<Void> deleteReviewImage(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long imageId
    ) {
        reviewService.deleteReviewImage(authUser, imageId);
        return Response.empty();
    }

    /**
     * 리뷰 수정 API
     */
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
    @Secured({USER, ADMIN})
    @DeleteMapping("/{reviewId}")
    public Response<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        reviewService.deleteReview(reviewId, authUser);
        return Response.empty();
    }

}
