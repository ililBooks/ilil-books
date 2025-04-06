package com.example.ililbooks.domain.review.controller;

import com.example.ililbooks.domain.review.dto.request.ReviewCreateRequest;
import com.example.ililbooks.domain.review.dto.response.ReviewResponse;
import com.example.ililbooks.domain.review.service.ReviewService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
