package com.example.ililbooks.domain.review.service;

import com.example.ililbooks.domain.book.service.BookService;
import com.example.ililbooks.domain.review.dto.request.ReviewCreateRequest;
import com.example.ililbooks.domain.review.dto.request.ReviewUpdateRequest;
import com.example.ililbooks.domain.review.dto.response.ReviewCreateResponse;
import com.example.ililbooks.domain.review.entity.Review;
import com.example.ililbooks.domain.review.entity.ReviewImage;
import com.example.ililbooks.domain.review.repository.ImageReviewRepository;
import com.example.ililbooks.domain.review.repository.ReviewRepository;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.ForbiddenException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.global.image.service.S3ImageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.example.ililbooks.domain.book.service.BookReadServiceTest.*;
import static com.example.ililbooks.domain.book.service.BookServiceTest.IMAGE_REQUEST;
import static com.example.ililbooks.domain.review.service.ReviewDeleteServiceTest.*;
import static com.example.ililbooks.global.exception.ErrorMessage.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ImageReviewRepository imageReviewRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookService bookService;

    @InjectMocks
    private ReviewService reviewService;


    //request
    public static final ReviewCreateRequest REVIEW_CREATE_REQUEST = new ReviewCreateRequest(
            TEST_BOOK_ID,
            5,
            "리뷰"
    );

    public static final ReviewUpdateRequest REVIEW_UPDATE_REQUEST = new ReviewUpdateRequest(
            4,
            "리뷰 수정"
    );

    //review
    public static final Long TEST_REVIEW_IMAGE_ID = 1L;

    //response
    public static final ReviewCreateResponse REVIEW_RESPONSE = ReviewCreateResponse.of(
            TEST_REVIEW
    );

    @Test
    void 유저가_존재하지_않아_리뷰_등록_실패() {
        //given
        given(userService.findByIdOrElseThrow(anyLong())).willThrow(new NotFoundException());

        //when & then
        assertThrows(NotFoundException.class,
                () -> reviewService.createReview(TEST_AUTH_USER, REVIEW_CREATE_REQUEST),
                USER_ID_NOT_FOUND.getMessage()
        );
    }

    @Test
    void 책이_존재하지_않아서_리뷰_등록_실패() {
        //given
        given(userService.findByIdOrElseThrow(anyLong())).willReturn(TEST_EMAIL_USERS);
        given(bookService.findBookByIdOrElseThrow(anyLong())).willThrow(new NotFoundException());

        //when & then
        assertThrows(NotFoundException.class,
                () -> reviewService.createReview(TEST_AUTH_USER, REVIEW_CREATE_REQUEST),
                NOT_FOUND_BOOK.getMessage()
        );
    }

    @Test
    void 리뷰가_이미_등록되어_있어서_등록_실패() {
        //given
        ReflectionTestUtils.setField(TEST_EMAIL_USERS,"id", TEST_USER_ID1);
        ReflectionTestUtils.setField(TEST_BOOK, "id", TEST_BOOK_ID);

        given(userService.findByIdOrElseThrow(anyLong())).willReturn(TEST_EMAIL_USERS);
        given(bookService.findBookByIdOrElseThrow(anyLong())).willReturn(TEST_BOOK);


        given(reviewRepository.existsByBookIdAndUsersId(anyLong(), anyLong())).willReturn(true);

        //when & then
        assertThrows(BadRequestException.class,
                () -> reviewService.createReview(TEST_AUTH_USER, REVIEW_CREATE_REQUEST),
                DUPLICATE_REVIEW.getMessage()
        );
    }

    @Test
    void 리뷰_등록_성공() {
        //given
        ReflectionTestUtils.setField(TEST_EMAIL_USERS,"id", TEST_USER_ID1);
        ReflectionTestUtils.setField(TEST_BOOK, "id", TEST_BOOK_ID);
        ReflectionTestUtils.setField(TEST_REVIEW, "id", TEST_REVIEW_ID);

        given(userService.findByIdOrElseThrow(anyLong())).willReturn(TEST_EMAIL_USERS);
        given(bookService.findBookByIdOrElseThrow(anyLong())).willReturn(TEST_BOOK);
        given(reviewRepository.existsByBookIdAndUsersId(anyLong(), anyLong())).willReturn(false);
        given(reviewRepository.save(any(Review.class))).willReturn(TEST_REVIEW);

        //when
        ReviewCreateResponse result = reviewService.createReview(TEST_AUTH_USER, REVIEW_CREATE_REQUEST);

        //then
        assertEquals(REVIEW_RESPONSE.id(), result.id());
    }

    @Test
    void 리뷰가_존재하지_않아_리뷰_이미지_업로드_실패() {
        //given
        given(reviewRepository.findById(anyLong())).willThrow(new NotFoundException());

        //when & then
        assertThrows(NotFoundException.class,
                () -> reviewService.uploadReviewImage(TEST_AUTH_USER, TEST_REVIEW_ID, IMAGE_REQUEST),
                NOT_FOUND_REVIEW.getMessage()
        );
    }

    @Test
    void 자신이_등록한_리뷰_이미지가_아니라서_업로드_실패() {
        //given
        ReflectionTestUtils.setField(TEST_EMAIL_USERS, "id", TEST_USER_ID1);
        ReflectionTestUtils.setField(TEST_AUTH_USER, "userId", TEST_USER_ID2);

        given(reviewRepository.findById(anyLong())).willReturn(Optional.ofNullable(TEST_REVIEW));

        //when & then
        assertThrows(ForbiddenException.class,
                () -> reviewService.uploadReviewImage(TEST_AUTH_USER, TEST_REVIEW_ID, IMAGE_REQUEST),
                CANNOT_UPDATE_OTHERS_REVIEW_IMAGE.getMessage()
        );

    }

    @Test
    void 입력된_위치에_이미_이미지가_존재하여_이미지_업로드_실패() {
        //given
        ReflectionTestUtils.setField(TEST_EMAIL_USERS, "id", TEST_USER_ID1);
        ReflectionTestUtils.setField(TEST_AUTH_USER, "userId", TEST_USER_ID1);
        ReflectionTestUtils.setField(TEST_REVIEW_IMAGE, "id", TEST_REVIEW_IMAGE_ID);

        given(reviewRepository.findById(anyLong())).willReturn(Optional.ofNullable(TEST_REVIEW));
        given(imageReviewRepository.existsByReviewIdAndPositionIndex(anyLong(), anyInt())).willReturn(true);

        //when & then
        assertThrows(BadRequestException.class,
                () -> reviewService.uploadReviewImage(TEST_AUTH_USER, TEST_REVIEW_ID, IMAGE_REQUEST),
                DUPLICATE_POSITION_INDEX.getMessage()
        );
    }

    @Test
    void 등록된_이미지의_개수_5개_초과로_이미지_업로드_실패() {
        //given
        ReflectionTestUtils.setField(TEST_EMAIL_USERS, "id", TEST_USER_ID1);
        ReflectionTestUtils.setField(TEST_AUTH_USER, "userId", TEST_USER_ID1);

        given(reviewRepository.findById(anyLong())).willReturn(Optional.ofNullable(TEST_REVIEW));

        given(imageReviewRepository.countByReviewId(anyLong())).willReturn(6);

        //when & then
        assertThrows(BadRequestException.class,
                () -> reviewService.uploadReviewImage(TEST_AUTH_USER, TEST_BOOK_ID, IMAGE_REQUEST),
                IMAGE_UPLOAD_LIMIT_OVER.getMessage()
        );
    }

    @Test
    void 리뷰_이미지_등록_성공() {
        //given
        ReflectionTestUtils.setField(TEST_EMAIL_USERS, "id", TEST_USER_ID1);
        ReflectionTestUtils.setField(TEST_AUTH_USER, "userId", TEST_USER_ID1);

        given(reviewRepository.findById(anyLong())).willReturn(Optional.ofNullable(TEST_REVIEW));
        given(imageReviewRepository.countByReviewId(anyLong())).willReturn(2);

        //when
        reviewService.uploadReviewImage(TEST_AUTH_USER, TEST_BOOK_ID, IMAGE_REQUEST);

        //then
        verify(imageReviewRepository).save(any(ReviewImage.class));
    }


    @Test
    void 리뷰가_존재하지_않아_리뷰_수정_실패() {
        //given
        given(reviewRepository.findById(anyLong())).willThrow(new NotFoundException());

        //when & then
        assertThrows(NotFoundException.class,
                () -> reviewService.updateReview(TEST_REVIEW_ID, TEST_AUTH_USER, REVIEW_UPDATE_REQUEST),
                NOT_FOUND_REVIEW.getMessage()
        );
    }

    @Test
    void 자신이_등록한_리뷰가_아니라서_수정_실패() {
        //given
        ReflectionTestUtils.setField(TEST_REVIEW, "id", TEST_REVIEW_ID);
        ReflectionTestUtils.setField(TEST_EMAIL_USERS, "id", TEST_USER_ID1);
        ReflectionTestUtils.setField(TEST_AUTH_USER, "userId", TEST_USER_ID2);

        given(reviewRepository.findById(anyLong())).willReturn(Optional.of(TEST_REVIEW));

        //when & then
        assertThrows(ForbiddenException.class,
                () -> reviewService.updateReview(TEST_REVIEW_ID, TEST_AUTH_USER, REVIEW_UPDATE_REQUEST),
                CANNOT_UPDATE_OTHERS_REVIEW.getMessage()
        );
    }

    @Test
    void 리뷰_수정_성공() {
        //given
        setUpReviewImageAndUser();

        given(reviewRepository.findById(anyLong())).willReturn(Optional.of(TEST_REVIEW));

        //when
        reviewService.updateReview(TEST_REVIEW_ID, TEST_AUTH_USER, REVIEW_UPDATE_REQUEST);

        //then
        assertEquals(REVIEW_UPDATE_REQUEST.rating(), TEST_REVIEW.getRating());
        assertEquals(REVIEW_UPDATE_REQUEST.comments(), TEST_REVIEW.getComments());

    }

    private static void setUpReviewImageAndUser() {
        ReflectionTestUtils.setField(TEST_REVIEW_IMAGE, "id", TEST_REVIEW_IMAGE_ID);
        ReflectionTestUtils.setField(TEST_EMAIL_USERS, "id", TEST_USER_ID1);
        ReflectionTestUtils.setField(TEST_AUTH_USER, "userId", TEST_USER_ID1);
    }
}