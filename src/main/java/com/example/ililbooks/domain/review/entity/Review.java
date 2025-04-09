package com.example.ililbooks.domain.review.entity;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.review.dto.request.ReviewCreateRequest;
import com.example.ililbooks.domain.review.dto.request.ReviewUpdateRequest;
import com.example.ililbooks.domain.user.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    private int rating;

    private String comments;

    @Builder
    private Review(Long id, Users users, Book book, int rating, String comments) {
        this.id = id;
        this.users = users;
        this.book = book;
        this.rating = rating;
        this.comments = comments;
    }

    //TODO dto빼고 인자로 넣기
    public static Review of(Users users, Book book, ReviewCreateRequest reviewCreateRequest) {
        return Review.builder()
                .users(users)
                .book(book)
                .rating(reviewCreateRequest.getRating())
                .comments(reviewCreateRequest.getComments())
                .build();
    }

    public void updateReview(ReviewUpdateRequest reviewUpdateRequest) {
        this.rating = reviewUpdateRequest.getRating();
        this.comments = reviewUpdateRequest.getComments();
    }
}
