package com.example.ililbooks.domain.cart.dto.response;

import com.example.ililbooks.domain.cart.entity.CartItem;
import lombok.Builder;

public record CartItemResponse(Long bookId, int quantity) {

    @Builder
    public CartItemResponse {
    }

    public static CartItemResponse of(CartItem cartItem) {
        return CartItemResponse.builder()
                .bookId(cartItem.getBookId())
                .quantity(cartItem.getQuantity())
                .build();
    }
}
