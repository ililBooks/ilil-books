package com.example.ililbooks.domain.cart.entity;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.enums.LimitedType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CartItem {

    private Long bookId;
    private int quantity;
    private String title;
    private String author;
    private BigDecimal price;
    private LimitedType limitedType;

    @Builder
    private CartItem(Long bookId, int quantity, String title, String author, BigDecimal price, LimitedType limitedType) {
        this.bookId = bookId;
        this.quantity = quantity;
        this.title = title;
        this.author = author;
        this.price = price;
        this.limitedType = limitedType;
    }

    public static CartItem of(Book book, int quantity) {
        return CartItem.builder()
                .bookId(book.getId())
                .quantity(quantity)
                .title(book.getTitle())
                .author(book.getAuthor())
                .price(book.getPrice())
                .limitedType(book.getLimitedType())
                .build();
    }

    public void changeQuantity(int quantity) {
        this.quantity += quantity;
    }
}
