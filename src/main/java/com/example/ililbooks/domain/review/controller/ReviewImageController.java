package com.example.ililbooks.domain.review.controller;

import com.example.ililbooks.domain.review.service.ReviewService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
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
public class ReviewImageController {
    private final ReviewService reviewService;

    /**
     * 리뷰 이미지 삭제 API
     */
    @Secured({USER, ADMIN})
    @DeleteMapping("/{imageId}")
    public Response<Void> deleteReviewImage(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long imageId
    ) {
        reviewService.deleteReviewImage(authUser, imageId);
        return Response.empty();
    }
}
