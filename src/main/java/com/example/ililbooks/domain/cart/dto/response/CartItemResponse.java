package com.example.ililbooks.domain.cart.dto.response;

import com.example.ililbooks.domain.cart.entity.CartItem;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CartItemResponse {

    private Long bookId;
    private int quantity;

    @Builder
    private CartItemResponse(Long bookId, int quantity) {
        this.bookId = bookId;
        this.quantity = quantity;
    }

    public static CartItemResponse of(CartItem cartItem) {
        return CartItemResponse.builder()
                .bookId(cartItem.getBookId())
                .quantity(cartItem.getQuantity())
                .build();
    }
}
