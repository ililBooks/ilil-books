package com.example.ililbooks.domain.review.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.service.BookService;
import com.example.ililbooks.domain.review.dto.request.ReviewCreateRequest;
import com.example.ililbooks.domain.review.dto.request.ReviewUpdateRequest;
import com.example.ililbooks.domain.review.dto.response.ReviewCreateResponse;
import com.example.ililbooks.domain.review.entity.Review;
import com.example.ililbooks.domain.review.entity.ReviewImage;
import com.example.ililbooks.domain.review.repository.ImageReviewRepository;
import com.example.ililbooks.domain.review.repository.ReviewRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.ForbiddenException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.global.image.dto.request.ImageRequest;
import com.example.ililbooks.global.image.service.S3ImageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.ililbooks.domain.user.enums.UserRole.isUser;
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
    public ReviewCreateResponse createReview(AuthUser authUser, ReviewCreateRequest reviewCreateRequest) {

        Users users = userService.findByIdOrElseThrow(authUser.getUserId());
        Book book = bookService.findBookByIdOrElseThrow(reviewCreateRequest.bookId());

        //이미 리뷰를 등록한 경우
        if (reviewRepository.existsByBookIdAndUsersId(book.getId(), users.getId())) {
            throw new BadRequestException(DUPLICATE_REVIEW.getMessage());
        }

        Review review = Review.of(
                users,
                book,
                reviewCreateRequest.rating(),
                reviewCreateRequest.comments()
        );
        Review savedReview = reviewRepository.save(review);

        return ReviewCreateResponse.of(savedReview);
    }

    @Transactional
    public void uploadReviewImage(AuthUser authUser, Long reviewId, ImageRequest imageRequest) {
        Review review = findReviewByIdOrElseThrow(reviewId);

        if (!review.getUsers().getId().equals(authUser.getUserId())) {
            throw new ForbiddenException(CANNOT_UPDATE_OTHERS_REVIEW_IMAGE.getMessage());
        }

        //해당 순서의 이미지가 이미 존재하는 경우
        if (imageReviewRepository.existsByReviewIdAndPositionIndex(review.getId(), imageRequest.positionIndex())) {
            throw new BadRequestException(DUPLICATE_POSITION_INDEX.getMessage());
        }

        ReviewImage reviewImage = ReviewImage.of(review, imageRequest.imageUrl(), imageRequest.fileName(),imageRequest.extension(), imageRequest.positionIndex());

        //등록 개수 초과
        if ( imageReviewRepository.countByReviewId(reviewImage.getReview().getId()) >= 5) {
            throw new BadRequestException(IMAGE_UPLOAD_LIMIT_OVER.getMessage());
        }

        imageReviewRepository.save(reviewImage);
    }

    @Transactional
    public void updateReview(Long reviewId, AuthUser authUser, ReviewUpdateRequest reviewUpdateRequest) {

        Review review = findReviewByIdOrElseThrow(reviewId);

        //다른 사람의 리뷰를 수정하려고 하는 경우
        if (!review.getUsers().getId().equals(authUser.getUserId()) && isUser(authUser)) {
            throw new ForbiddenException(CANNOT_UPDATE_OTHERS_REVIEW.getMessage());
        }

        review.updateReview(reviewUpdateRequest.rating(), reviewUpdateRequest.comments());
    }

    public Review findReviewByIdOrElseThrow(Long reviewId) {
        return reviewRepository.findById(reviewId).orElseThrow(()-> new NotFoundException(NOT_FOUND_REVIEW.getMessage()));
    }
}
