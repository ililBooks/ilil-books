package com.example.ililbooks.domain.cart.dto.response;

import lombok.Builder;

import java.util.List;

public record CartResponse(Long userId, List<CartItemResponse> items) {
    @Builder
    public CartResponse {
    }

    public static CartResponse of(Long userId, List<CartItemResponse> items) {
        return CartResponse.builder()
                .userId(userId)
                .items(items)
                .build();
    }
}
