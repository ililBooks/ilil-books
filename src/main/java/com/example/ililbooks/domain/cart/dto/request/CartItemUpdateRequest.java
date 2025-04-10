package com.example.ililbooks.domain.cart.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

import static com.example.ililbooks.global.dto.ValidationMessage.NOT_EMPTY_SHOPPING_CART_ITEM;

public record CartItemUpdateRequest(
        @NotEmpty(message = NOT_EMPTY_SHOPPING_CART_ITEM) List<CartItemRequest> cartItemList) {

}
