package com.example.ililbooks.domain.review.service;

import com.example.ililbooks.domain.review.entity.Review;
import com.example.ililbooks.domain.review.entity.ReviewImage;
import com.example.ililbooks.domain.review.repository.ImageReviewRepository;
import com.example.ililbooks.domain.review.repository.ReviewRepository;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.ForbiddenException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.global.image.service.S3ImageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static com.example.ililbooks.domain.book.service.BookReadServiceTest.*;
import static com.example.ililbooks.global.exception.ErrorMessage.CANNOT_DELETE_OTHERS_REVIEW;
import static com.example.ililbooks.global.exception.ErrorMessage.NOT_FOUND_REVIEW;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ReviewDeleteServiceTest {
    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private S3ImageService s3ImageService;

    @Mock
    private ImageReviewRepository imageReviewRepository;

    @InjectMocks
    private ReviewDeleteService reviewDeleteService;

    //users
    public static final Long TEST_USER_ID1 = 1L;
    public static final Long TEST_USER_ID2 = 2L;
    public static final AuthUser TEST_AUTH_USER = AuthUser.builder()
            .userId(TEST_USER_ID1)
            .email(TEST_EMAIL_USERS.getEmail())
            .nickname(TEST_EMAIL_USERS.getNickname())
            .role(TEST_EMAIL_USERS.getUserRole())
            .build();

    //review
    public static final ReviewImage TEST_REVIEW_IMAGE = ReviewImage.builder()
            .review(TEST_REVIEW)
            .imageUrl("imageUrl.png")
            .fileName("imagerUrl")
            .extension("png")
            .build();

    public static final List<Review> TEST_LIST_REVIEW = List.of(
            TEST_REVIEW
    );

    public static final List<ReviewImage> TEST_LIST_REVIEW_IMAGE = List.of(
            TEST_REVIEW_IMAGE
    );

    public static final List<String> TEST_LIST_REVIEW_IMAGE_FILENAME = TEST_LIST_REVIEW_IMAGE.stream()
            .map(ReviewImage::getFileName)
            .toList();


    @Test
    void 리뷰가_존재하지_않아_삭제_실패() {
        //given
        given(reviewRepository.findReviewById(anyLong())).willThrow(new NotFoundException());

        //when & then
        assertThrows(NotFoundException.class,
                () -> reviewDeleteService.deleteReview(TEST_REVIEW_ID, TEST_AUTH_USER),
                NOT_FOUND_REVIEW.getMessage()
        );
    }

    @Test
    void 자신이_작성한_리뷰가_가_아니어서_삭제_실패() {
        //given
        ReflectionTestUtils.setField(TEST_REVIEW, "id", TEST_REVIEW_ID);
        ReflectionTestUtils.setField(TEST_EMAIL_USERS, "id", TEST_USER_ID1);
        ReflectionTestUtils.setField(TEST_AUTH_USER, "userId", TEST_USER_ID2);

        given(reviewRepository.findReviewById(TEST_REVIEW_ID)).willReturn(Optional.of(TEST_REVIEW));

        //when & then
        assertThrows(ForbiddenException.class,
                () -> reviewDeleteService.deleteReview(TEST_REVIEW_ID, TEST_AUTH_USER),
                CANNOT_DELETE_OTHERS_REVIEW.getMessage()
        );
    }

    @Test
    void 리뷰_삭제_성공() {
        //given
        ReflectionTestUtils.setField(TEST_REVIEW, "id", TEST_REVIEW_ID);
        ReflectionTestUtils.setField(TEST_EMAIL_USERS, "id", TEST_USER_ID1);

        given(reviewRepository.findReviewById(anyLong())).willReturn(Optional.of(TEST_REVIEW));
        given(imageReviewRepository.findAllReviewImageById(anyLong())).willReturn(TEST_LIST_REVIEW_IMAGE);

        //when
        reviewDeleteService.deleteReview(TEST_REVIEW_ID, TEST_AUTH_USER);

        //then
        verify(s3ImageService, times(TEST_LIST_REVIEW_IMAGE.size())).deleteImage(anyString());
        verify(imageReviewRepository).deleteAllByReviewId(anyLong());
        verify(reviewRepository).delete(any(Review.class));
    }

    @Test
    void 모든_리뷰_삭제_성공() {
        //given
        ReflectionTestUtils.setField(TEST_BOOK, "id", TEST_BOOK_ID);
        ReflectionTestUtils.setField(TEST_REVIEW, "id", TEST_REVIEW_ID);

        given(reviewRepository.findReviewsByBookId(anyLong())).willReturn(TEST_LIST_REVIEW);
        given(imageReviewRepository.findFileNameByReviewId(anyLong())).willReturn(TEST_LIST_REVIEW_IMAGE_FILENAME);

        //when
        reviewDeleteService.deleteAllReviewByBookId(TEST_BOOK_ID);

        //then
        verify(s3ImageService,times(TEST_LIST_REVIEW_IMAGE_FILENAME.size())).deleteImage(anyString());
        verify(imageReviewRepository, times(TEST_LIST_REVIEW_IMAGE_FILENAME.size())).deleteByFileName(anyString());
        verify(reviewRepository).deleteAllByBookId(anyLong());
    }
}