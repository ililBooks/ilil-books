package com.example.ililbooks.domain.book.entity;

import com.example.ililbooks.client.book.dto.BookApiResponse;
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

    private String publisher;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private SaleStatus saleStatus;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private LimitedType limitedType;

    private boolean isDeleted;

    @Builder
    private Book(Long id, Users users, String title, String author, BigDecimal price, String category, int stock, String isbn, String publisher, SaleStatus saleStatus, LimitedType limitedType) {
        this.id = id;
        this.users = users;
        this.title = title;
        this.author = author;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.isbn = isbn;
        this.publisher = publisher;
        this.saleStatus = saleStatus;
        this.limitedType = limitedType;
        this.isDeleted = false;
    }

    public static Book of(Users users, String title, String author, BigDecimal price, String category, int stock, String isbn, String publisher ) {
        return Book.builder()
                .users(users)
                .title(title)
                .author(author)
                .price(price)
                .category(category)
                .stock(stock)
                .isbn(isbn)
                .publisher(publisher)
                .saleStatus(ON_SALE)
                .limitedType(REGULAR)
                .build();
    }

    public static Book of(Users users, BookApiResponse book, BigDecimal price, int stock) {
        return Book.builder()
                .users(users)
                .title(book.title().replaceAll("<[^>]*>", ""))  //Entity 내부에서 파싱하는건 대표적인 SRP 위반이에요
                .author(book.author().replaceAll("<[^>]*>", ""))
                .price(price)
                .category(book.category())
                .stock(stock)
                .isbn(book.isbn())
                .publisher(book.publisher().replaceAll("<[^>]*>", ""))
                .saleStatus(ON_SALE)
                .limitedType(REGULAR)
                .build();
    }

    public void updateBook(String title, String author, BigDecimal price, String category, int stock, String saleStatus, String limitedType) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.saleStatus = SaleStatus.valueOf(saleStatus);
        this.limitedType = LimitedType.valueOf(limitedType);
    }

    public int decreaseStock(int quantity) {
        this.stock -= quantity;
        return stock;
    }

    public void deleteBook() {
        this.isDeleted = true;
    }

    public void rollbackStock(int quantity) {
        this.stock += quantity;
    }
}
