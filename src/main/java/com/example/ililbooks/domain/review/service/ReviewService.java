package com.example.ililbooks.domain.review.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.service.BookService;
import com.example.ililbooks.domain.review.dto.request.ReviewCreateRequest;
import com.example.ililbooks.domain.review.dto.request.ReviewUpdateRequest;
import com.example.ililbooks.domain.review.dto.response.ReviewResponse;
import com.example.ililbooks.domain.review.entity.Review;
import com.example.ililbooks.domain.review.repository.ReviewRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.global.image.entity.ReviewImage;
import com.example.ililbooks.global.image.repository.ImageReviewRepository;
import com.example.ililbooks.global.image.service.S3ImageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.USER;
import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ImageReviewRepository imageReviewRepository;
    private final UserService userService;
    private final BookService bookService;
    private final S3ImageService s3ImageService;

    @Transactional
    public ReviewResponse createReview(AuthUser authUser, ReviewCreateRequest reviewCreateRequest) {

        Users findUsers = userService.findByIdOrElseThrow(authUser.getUserId());
        Book findBook = bookService.findBookByIdOrElseThrow(reviewCreateRequest.getBookId());

        //이미 리뷰를 등록한 경우
        if (reviewRepository.existsByBookIdAndUsersId(findBook.getId(), findUsers.getId())) {
            throw new BadRequestException(DUPLICATE_REVIEW.getMessage());
        }

        Review review = Review.of(findUsers, findBook, reviewCreateRequest);
        Review savedReview = reviewRepository.save(review);

        return ReviewResponse.of(savedReview);
    }

    public void uploadReviewImage(Long reviewId, String imageUrl) {
        Review findReview = findReviewByIdOrElseThrow(reviewId);
        ReviewImage reviewImage = ReviewImage.of(findReview, imageUrl);

        imageReviewRepository.save(reviewImage);
    }

    public void deleteReviewImage(AuthUser authUser, Long imageId) {

        //리뷰에 이미지가 없는 경우
        ReviewImage reviewImage  = imageReviewRepository.findReviewImageById(imageId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_REVIEW.getMessage()));

        //사용자가 다른 사람의 이미지를 삭제하려는 경우
        if (!authUser.getUserId().equals(reviewImage.getReview().getUsers().getId()) && USER.equals(authUser.getAuthorities().iterator().next().getAuthority())) {
            throw new BadRequestException(CANNOT_DELETE_OTHERS_REVIEW.getMessage());
        }

        s3ImageService.deleteImage(reviewImage.getFileName());
        imageReviewRepository.delete(reviewImage);
    }

    @Transactional
    public void updateReview(Long reviewId, AuthUser authUser, ReviewUpdateRequest reviewUpdateRequest) {

        Review findReview = findReviewByIdOrElseThrow(reviewId);

        //다른 사람의 리뷰를 수정하려고 하는 경우
        if (!findReview.getUsers().getId().equals(authUser.getUserId())) {
            throw new BadRequestException(CANNOT_UPDATE_OTHERS_REVIEW.getMessage());
        }

        findReview.updateReview(reviewUpdateRequest);
    }

    @Transactional
    public void deleteReview(Long reviewId, AuthUser authUser) {
        Review findReview = findReviewByIdOrElseThrow(reviewId);

        //다른 사람의 리뷰를 삭제하려고 하는 경우 (ADMIN은 해당되지 않음)
        String userRole = authUser.getAuthorities().iterator().next().getAuthority();

        if (!findReview.getUsers().getId().equals(authUser.getUserId()) && USER.equals(userRole)) {
            throw new BadRequestException(CANNOT_DELETE_OTHERS_REVIEW.getMessage());
        }

        reviewRepository.delete(findReview);
    }

    public Review findReviewByIdOrElseThrow(Long reviewId) {
        return reviewRepository.findById(reviewId).orElseThrow(()-> new NotFoundException(NOT_FOUND_REVIEW.getMessage()));
    }
}
