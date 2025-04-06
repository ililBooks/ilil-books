package com.example.ililbooks.domain.review.entity;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.review.dto.request.ReviewUpdateRequest;
import com.example.ililbooks.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    private int rating;

    private String comments;

    @Builder
    private Review(Long id, User user, Book book, int rating, String comments) {
        this.id = id;
        this.user = user;
        this.book = book;
        this.rating = rating;
        this.comments = comments;
    }

    public void updateReview(ReviewUpdateRequest reviewUpdateRequest) {
        this.rating = reviewUpdateRequest.getRating();
        this.comments = reviewUpdateRequest.getComments();
    }
}
