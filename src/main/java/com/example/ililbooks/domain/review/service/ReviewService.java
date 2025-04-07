package com.example.ililbooks.domain.review.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.service.BookService;
import com.example.ililbooks.domain.review.dto.request.ReviewCreateRequest;
import com.example.ililbooks.domain.review.dto.request.ReviewUpdateRequest;
import com.example.ililbooks.domain.review.dto.response.ReviewResponse;
import com.example.ililbooks.domain.review.entity.Review;
import com.example.ililbooks.domain.review.repository.ReviewRepository;
import com.example.ililbooks.domain.user.entity.User;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.USER;
import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final BookService bookService;

    @Transactional
    public ReviewResponse createReview(AuthUser authUser, ReviewCreateRequest reviewCreateRequest) {

        User findUser = userService.getUserById(authUser.getUserId());
        Book findBook = bookService.findBookById(reviewCreateRequest.getBookId());

        //이미 리뷰를 등록한 경우
        if (reviewRepository.existsByBookIdAndUserId(findBook.getId(), findUser.getId())) {
            throw new BadRequestException(DUPLICATE_REVIEW.getMessage());
        }

        Review review = Review.builder()
                .user(findUser)
                .book(findBook)
                .rating(reviewCreateRequest.getRating())
                .comments(reviewCreateRequest.getComments())
                .build();

        Review savedReview = reviewRepository.save(review);

        return ReviewResponse.of(savedReview);
    }

    @Transactional
    public void updateReview(Long reviewId, AuthUser authUser, ReviewUpdateRequest reviewUpdateRequest) {

        Review findReview = getReview(reviewId);

        //다른 사람의 리뷰를 수정하려고 하는 경우
        if (!findReview.getUser().getId().equals(authUser.getUserId())) {
            throw new BadRequestException(CANNOT_UPDATE_OTHERS_REVIEW.getMessage());
        }

        findReview.updateReview(reviewUpdateRequest);
    }

    @Transactional
    public void deleteReview(Long reviewId, AuthUser authUser) {
        Review findReview = getReview(reviewId);

        //다른 사람의 리뷰를 삭제하려고 하는 경우 (ADMIN은 해당되지 않음)
        String userRole = authUser.getAuthorities().iterator().next().getAuthority();

        if (!findReview.getUser().getId().equals(authUser.getUserId()) && USER.equals(userRole)) {
            throw new BadRequestException(CANNOT_DELETE_OTHERS_REVIEW.getMessage());
        }

        reviewRepository.delete(findReview);
    }

    public Review getReview(Long reviewId) {
        return reviewRepository.findById(reviewId).orElseThrow(()-> new NotFoundException(NOT_FOUND_REVIEW.getMessage()));
    }
}
