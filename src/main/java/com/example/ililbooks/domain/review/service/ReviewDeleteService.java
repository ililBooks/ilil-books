package com.example.ililbooks.domain.review.service;

import com.example.ililbooks.domain.review.entity.Review;
import com.example.ililbooks.domain.review.entity.ReviewImage;
import com.example.ililbooks.domain.review.repository.ImageReviewRepository;
import com.example.ililbooks.domain.review.repository.ReviewRepository;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.ForbiddenException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.global.image.service.S3ImageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.ililbooks.domain.user.enums.UserRole.isUser;
import static com.example.ililbooks.global.exception.ErrorMessage.CANNOT_DELETE_OTHERS_REVIEW;
import static com.example.ililbooks.global.exception.ErrorMessage.NOT_FOUND_REVIEW;

@Service
@RequiredArgsConstructor
public class ReviewDeleteService {

    private final ReviewRepository reviewRepository;
    private final S3ImageService s3ImageService;
    private final ImageReviewRepository imageReviewRepository;

    @Transactional
    public void deleteReviews(Long reviewId, AuthUser authUser) {
        Review review = reviewRepository.findReviewById(reviewId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_REVIEW.getMessage()));

        if (!review.getUsers().getId().equals(authUser.getUserId()) && isUser(authUser)) {
            throw new ForbiddenException(CANNOT_DELETE_OTHERS_REVIEW.getMessage());
        }

        List<ReviewImage> reviewImages = imageReviewRepository.findAllReviewImageById(review.getId());

        reviewImages.forEach(reviewImage ->
                    s3ImageService.deleteImage(reviewImage.getFileName())
        );

        imageReviewRepository.deleteAllByReviewId(review.getId());
        reviewRepository.delete(review);
    }

    @Transactional
    public void deleteReviewImage(AuthUser authUser, Long imageId) {

        ReviewImage reviewImage  = findReviewImage(imageId);
        Long userId = imageReviewRepository.findUserIdByReviewImageId(reviewImage.getId());

        //사용자가 다른 사람의 이미지를 삭제하려는 경우
        if (!authUser.getUserId().equals(userId) && isUser(authUser)) {
            throw new ForbiddenException(CANNOT_DELETE_OTHERS_REVIEW.getMessage());
        }

        s3ImageService.deleteImage(reviewImage.getFileName());
        imageReviewRepository.delete(reviewImage);
    }

    @Transactional
    public void deleteAllReviewByBookId(Long bookId) {
        List<Review> reviews = reviewRepository.findReviewsByBookId(bookId);

        reviews.forEach(
                review -> {
                    List<String> fileNameList = imageReviewRepository.findFileNameByReviewId(review.getId());

                    fileNameList.forEach(fileName -> {
                                s3ImageService.deleteImage(fileName);
                                imageReviewRepository.deleteByFileName(fileName);
                            }

                    );

                }
        );

        reviewRepository.deleteAllByBookId(bookId);
    }

    private ReviewImage findReviewImage(Long imageId) {
        return imageReviewRepository.findReviewImageById(imageId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_REVIEW.getMessage()));
    }
}
