package com.example.ililbooks.domain.review.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.service.BookService;
import com.example.ililbooks.domain.review.dto.request.ReviewCreateRequest;
import com.example.ililbooks.domain.review.dto.request.ReviewUpdateRequest;
import com.example.ililbooks.domain.review.dto.response.ReviewResponse;
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
    public ReviewResponse createReview(AuthUser authUser, ReviewCreateRequest reviewCreateRequest) {

        Users users = userService.findByIdOrElseThrow(authUser.getUserId());
        Book book = bookService.findBookByIdOrElseThrow(reviewCreateRequest.getBookId());

        //이미 리뷰를 등록한 경우
        if (reviewRepository.existsByBookIdAndUsersId(book.getId(), users.getId())) {
            throw new BadRequestException(DUPLICATE_REVIEW.getMessage());
        }

        Review review = Review.of(
                users,
                book,
                reviewCreateRequest.getRating(),
                reviewCreateRequest.getComments()
        );
        Review savedReview = reviewRepository.save(review);

        return ReviewResponse.of(savedReview);
    }
    
    @Transactional
    public void uploadReviewImage(AuthUser authUser, Long reviewId, ImageRequest imageRequest) {
        Review review = findReviewByIdOrElseThrow(reviewId);

        if (!review.getUsers().getId().equals(authUser.getUserId())) {
            throw new ForbiddenException(CANNOT_UPDATE_OTHERS_REVIEW_IMAGE.getMessage());
        }

        ReviewImage reviewImage = ReviewImage.of(review, imageRequest.getImageUrl(), imageRequest.getFileName(),imageRequest.getExtension());

        //등록 개수 초과 
        if ( imageReviewRepository.countByReviewId(reviewImage.getReview().getId()) >= 5) {
            throw new BadRequestException(IMAGE_UPLOAD_LIMIT_OVER.getMessage());
        }

        imageReviewRepository.save(reviewImage);
    }

    @Transactional
    public void deleteReviewImage(AuthUser authUser, Long imageId) {

        ReviewImage reviewImage  = findReviewImage(imageId);

        //사용자가 다른 사람의 이미지를 삭제하려는 경우
        if (!authUser.getUserId().equals(reviewImage.getReview().getUsers().getId()) && isUser(authUser)) {
            throw new ForbiddenException(CANNOT_DELETE_OTHERS_REVIEW.getMessage());
        }

        s3ImageService.deleteImage(reviewImage.getFileName());
        imageReviewRepository.delete(reviewImage);
    }

    @Transactional
    public void updateReview(Long reviewId, AuthUser authUser, ReviewUpdateRequest reviewUpdateRequest) {

        Review review = findReviewByIdOrElseThrow(reviewId);

        //다른 사람의 리뷰를 수정하려고 하는 경우
        if (!review.getUsers().getId().equals(authUser.getUserId()) && isUser(authUser)) {
            throw new ForbiddenException(CANNOT_UPDATE_OTHERS_REVIEW.getMessage());
        }

        review.updateReview(reviewUpdateRequest.getRating(), reviewUpdateRequest.getComments());
    }

    public Review findReviewByIdOrElseThrow(Long reviewId) {
        return reviewRepository.findById(reviewId).orElseThrow(()-> new NotFoundException(NOT_FOUND_REVIEW.getMessage()));
    }

    public ReviewImage findReviewImage(Long imageId) {
        return imageReviewRepository.findReviewImageById(imageId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_REVIEW.getMessage()));
    }
}
