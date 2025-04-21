package com.example.ililbooks.domain.cart.entity;

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
    public CartItem(Long bookId, int quantity, String title, String author, BigDecimal price, LimitedType limitedType) {
        this.bookId = bookId;
        this.quantity = quantity;
        this.title = title;
        this.author = author;
        this.price = price;
        this.limitedType = limitedType;
    }

    public static CartItem of(Long bookId, int quantity, String title, String author, BigDecimal price, LimitedType limitedType) {
        return CartItem.builder()
                .bookId(bookId)
                .quantity(quantity)
                .title(title)
                .author(author)
                .price(price)
                .limitedType(limitedType)
                .build();
    }

    public void changeQuantity(int quantity) {
        this.quantity += quantity;
    }
}
