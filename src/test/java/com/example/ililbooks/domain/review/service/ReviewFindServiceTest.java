package com.example.ililbooks.domain.review.service;

import com.example.ililbooks.domain.review.dto.response.ReviewWithImagesResponse;
import com.example.ililbooks.domain.review.entity.Review;
import com.example.ililbooks.domain.review.repository.ImageReviewRepository;
import com.example.ililbooks.domain.review.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.example.ililbooks.domain.book.service.BookReadServiceTest.*;
import static com.example.ililbooks.domain.review.service.ReviewDeleteServiceTest.TEST_REVIEW_IMAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ReviewFindServiceTest {
    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ImageReviewRepository imageReviewRepository;

    @InjectMocks
    private ReviewFindService reviewFindService;

    public static final Page<Review> TEST_PAGE_REVIEW = new PageImpl<>(
            List.of(TEST_REVIEW),
            TEST_PAGEALBE,
            List.of(TEST_REVIEW).size()
    );

    @Test
    void 리뷰_전체_조회_성공() {
        //given
        given(reviewRepository.findAllByBookId(anyLong(), any(Pageable.class))).willReturn(TEST_PAGE_REVIEW);
        given(imageReviewRepository.findFirstByReviewId(anyLong())).willReturn(Optional.ofNullable(TEST_REVIEW_IMAGE));

        //when
        Page<ReviewWithImagesResponse> result = reviewFindService.getReviews(TEST_BOOK_ID, TEST_PAGEALBE);

        //then
        assertEquals(result.getTotalElements(), TEST_PAGE_REVIEW.getTotalElements());
    }

    @Test
    void 이미지를_제외한_리뷰_전체_조회_성공() {
        //given
        given(reviewRepository.findAllByBookId(anyLong(), any(Pageable.class))).willReturn(TEST_PAGE_REVIEW);
        given(imageReviewRepository.findFirstByReviewId(anyLong())).willReturn(Optional.empty());

        //when
        Page<ReviewWithImagesResponse> result = reviewFindService.getReviews(TEST_BOOK_ID, TEST_PAGEALBE);

        //then
        assertEquals(result.getTotalElements(), TEST_PAGE_REVIEW.getTotalElements());
    }

}