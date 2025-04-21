package com.example.ililbooks.domain.cart.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CartItem {

    private Long bookId;
    private int quantity;

    @Builder
    private CartItem(Long bookId, int quantity) {
        this.bookId = bookId;
        this.quantity = quantity;
    }

    public static CartItem of(Long bookId, int quantity) {
        return CartItem.builder()
                .bookId(bookId)
                .quantity(quantity)
                .build();
    }

    public void changeQuantity(int quantity) {
        this.quantity += quantity;
    }
}
