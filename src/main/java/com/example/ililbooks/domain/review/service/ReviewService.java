package com.example.ililbooks.domain.review.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.service.BookService;
import com.example.ililbooks.domain.review.dto.request.ReviewCreateRequest;
import com.example.ililbooks.domain.review.dto.response.ReviewResponse;
import com.example.ililbooks.domain.review.entity.Review;
import com.example.ililbooks.domain.review.repository.ReviewRepository;
import com.example.ililbooks.domain.user.entity.User;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.ililbooks.global.exception.ErrorMessage.DUPLICATE_REVIEW;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final BookService bookService;
    private final ReviewRepository reviewRepository;
    private final UserService userService;

    @Transactional
    public ReviewResponse createReview(AuthUser authUser, ReviewCreateRequest reviewCreateRequest) {

        //로그인한 유저
        User findUser = userService.getUserById(authUser.getUserId());

        Book findBook = bookService.getBookById(reviewCreateRequest.getBookId());

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
}
