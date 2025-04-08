package com.example.ililbooks.domain.cart.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class CartResponse {
    private Long userId;
    private List<CartItemResponse> items;

    @Builder
    private CartResponse(Long userId, List<CartItemResponse> items) {
        this.userId = userId;
        this.items = items;
    }

    public static CartResponse of(Long userId, List<CartItemResponse> items) {
        return CartResponse.builder()
                .userId(userId)
                .items(items)
                .build();
    }
}
