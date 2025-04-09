package com.example.ililbooks.domain.book.entity;

import com.example.ililbooks.client.dto.BookApiResponse;
import com.example.ililbooks.domain.book.dto.request.BookCreateRequest;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private Users users;

    private String title;

    private String author;

    private BigDecimal price;

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
    private Book(Users users, String title, String author, BigDecimal price, String category, int stock, String isbn) {
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

    public static Book of(Users users, BookCreateRequest bookCreateRequest) {
        return Book.builder()
                .users(users)
                .title(bookCreateRequest.getTitle())
                .author(bookCreateRequest.getAuthor())
                .price(bookCreateRequest.getPrice())
                .category(bookCreateRequest.getCategory())
                .stock(bookCreateRequest.getStock())
                .isbn(bookCreateRequest.getIsbn())
                .build();
    }

    public static Book of(Users users, BookApiResponse book, BigDecimal price, int stock) {
        return Book.builder()
                .users(users)
                .title(book.getTitle().replaceAll("<[^>]*>", ""))
                .author(book.getAuthor().replaceAll("<[^>]*>", ""))
                .price(price)
                .category(book.getCategory())
                .stock(stock)
                .isbn(book.getIsbn())
                .build();
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

    public int decreaseStock(int quantity) {
        this.stock -= quantity;
        return stock;
    }

    public void rollbackStock(int quantity) {
        this.stock += quantity;
    }
}
