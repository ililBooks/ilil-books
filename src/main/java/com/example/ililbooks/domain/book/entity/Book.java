package com.example.ililbooks.domain.book.entity;

import com.example.ililbooks.domain.book.dto.request.BookUpdateRequest;
import com.example.ililbooks.domain.book.enums.LimitedType;
import com.example.ililbooks.domain.book.enums.SaleStatus;
import com.example.ililbooks.domain.user.entity.User;
import com.example.ililbooks.global.entity.TimeStamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static com.example.ililbooks.domain.book.enums.LimitedType.REGULAR;
import static com.example.ililbooks.domain.book.enums.SaleStatus.ON_SALE;

@Getter
@Entity
@Table(name = "books")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String title;

    private String author;

    private BigDecimal price;

    private String category;

    private int stock;

    @Enumerated(EnumType.STRING)
    private SaleStatus saleStatus;

    @Enumerated(EnumType.STRING)
    private LimitedType limitedType;

    @Builder
    private Book(User user, String title, String author, BigDecimal price, String category, int stock) {
        this.user = user;
        this.title = title;
        this.author = author;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.saleStatus = ON_SALE;
        this.limitedType = REGULAR;
    }

    public void updateBook(BookUpdateRequest bookUpdateRequest) {
        this.title = bookUpdateRequest.getTitle();
        this.author = bookUpdateRequest.getAuthor();
        this.price = bookUpdateRequest.getPrice();
        this.category = bookUpdateRequest.getCategory();
        this.stock = bookUpdateRequest.getStock();
        this.saleStatus = bookUpdateRequest.getSaleStatus();
        this.limitedType = bookUpdateRequest.getLimitedType();
    }
}
