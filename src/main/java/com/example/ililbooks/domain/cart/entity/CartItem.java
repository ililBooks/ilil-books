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

    //TODO 패턴 피해야해요, 의존성 역전되어있는 케이스임
    //CartItem은 Dto로만 생성이 가능
    //CartItem of(bookId, quantity)
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
