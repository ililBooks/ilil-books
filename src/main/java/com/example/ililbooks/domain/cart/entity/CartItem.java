package com.example.ililbooks.domain.cart.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CartItem {

    private Long bookId;
    private int quantity;

    @Builder
    public CartItem(Long bookId, int quantity) {
        this.bookId = bookId;
        this.quantity = quantity;
    }

    public void updateQuantity(int quantity) {
        this.quantity += quantity;
    }
}
