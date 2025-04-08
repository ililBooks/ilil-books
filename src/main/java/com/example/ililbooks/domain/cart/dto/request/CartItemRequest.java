package com.example.ililbooks.domain.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import static com.example.ililbooks.global.dto.ValidationMessage.*;

@Getter
public class CartItemRequest {

    @NotNull(message = NOT_NULL_BOOK_ID)
    private Long bookId;

    @Min(value = 1, message = INVALID_BOOK_QUANTITY)
    private int quantity;
}
