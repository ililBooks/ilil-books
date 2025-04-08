package com.example.ililbooks.domain.cart.entity;

import com.example.ililbooks.domain.cart.dto.request.CartItemRequest;
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

    public static CartItem of(CartItemRequest cartItemRequest) {
        return CartItem.builder()
                .bookId(cartItemRequest.getBookId())
                .quantity(cartItemRequest.getQuantity())
                .build();
    }

    public void updateQuantity(int quantity) {
        this.quantity += quantity;
    }
}
