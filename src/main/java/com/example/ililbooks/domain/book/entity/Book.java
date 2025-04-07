package com.example.ililbooks.domain.book.entity;

import com.example.ililbooks.domain.book.dto.request.BookUpdateRequest;
import com.example.ililbooks.domain.book.enums.LimitedType;
import com.example.ililbooks.domain.book.enums.SaleStatus;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.global.entity.TimeStamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users users;

    private String title;

    private String author;

    private Long price;

    private String category;

    private int stock;

    // 책의 고유 번호
    @Column(unique = true)
    private String isbn;

    @Enumerated(EnumType.STRING)
    private SaleStatus saleStatus;

    @Enumerated(EnumType.STRING)
    private LimitedType limitedType;

    @Builder
    private Book(Users users, String title, String author, Long price, String category, int stock, String isbn) {
        this.users = users;
        this.title = title;
        this.author = author;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.isbn = isbn;
        this.saleStatus = ON_SALE;
        this.limitedType = REGULAR;
    }

    public void updateBook(BookUpdateRequest bookUpdateRequest) {
        this.title = bookUpdateRequest.getTitle();
        this.author = bookUpdateRequest.getAuthor();
        this.price = bookUpdateRequest.getPrice();
        this.category = bookUpdateRequest.getCategory();
        this.stock = bookUpdateRequest.getStock();
        this.saleStatus = SaleStatus.valueOf(bookUpdateRequest.getSaleStatus());
        this.limitedType = LimitedType.valueOf(bookUpdateRequest.getLimitedType());
    }
}
