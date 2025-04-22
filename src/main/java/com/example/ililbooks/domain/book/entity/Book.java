package com.example.ililbooks.domain.book.entity;

import com.example.ililbooks.client.book.dto.BookApiResponse;
import com.example.ililbooks.domain.book.enums.LimitedType;
import com.example.ililbooks.domain.book.enums.SaleStatus;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.global.entity.TimeStamped;
import com.example.ililbooks.global.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static com.example.ililbooks.domain.book.enums.LimitedType.REGULAR;
import static com.example.ililbooks.domain.book.enums.SaleStatus.ON_SALE;
import static com.example.ililbooks.global.exception.ErrorMessage.OUT_OF_STOCK;

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

    // 주문 - 낙관락
    @Version
    private Long version;

    @Builder
    private Book(Long id, Users users, String title, String author, BigDecimal price, String category, int stock, String isbn, String publisher, SaleStatus saleStatus, LimitedType limitedType, boolean isDeleted, Long version) {
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
        this.isDeleted = isDeleted;
        this.version = version;
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

    public static Book of(Users users, String title, String author, String publisher, String category, String isbn, BigDecimal price, int stock) {
        return Book.builder()
                .users(users)
                .title(title)
                .author(author)
                .publisher(publisher)
                .category(category)
                .isbn(isbn)
                .price(price)
                .stock(stock)
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

    public void deleteBook() {
        this.isDeleted = true;
    }

    public void decreaseStock(int quantity) {
        if (stock < quantity) {
            throw new BadRequestException(OUT_OF_STOCK.getMessage());
        }
        if (stock == quantity) {
            this.saleStatus = SaleStatus.SOLD_OUT;
        }
        this.stock -= quantity;
        this.version++;
    }

    public void increaseStoke(int quantity) {
        this.stock += quantity;

        if (this.stock > 0 && this.saleStatus == SaleStatus.SOLD_OUT) {
            this.saleStatus = SaleStatus.ON_SALE;
        }
    }
}
