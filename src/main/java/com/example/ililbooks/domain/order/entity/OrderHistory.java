package com.example.ililbooks.domain.order.entity;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.enums.LimitedType;
import com.example.ililbooks.global.entity.TimeStamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_histories")
public class OrderHistory extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    private String title;

    private String author;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private LimitedType limitedType;

    private int quantity;

    @Builder
    private OrderHistory(Long id, Order order, Book book, String title, String author, BigDecimal price, LimitedType limitedType, int quantity) {
        this.id = id;
        this.order = order;
        this.book = book;
        this.title = title;
        this.author = author;
        this.price = price;
        this.limitedType = limitedType;
        this.quantity = quantity;
    }
}
