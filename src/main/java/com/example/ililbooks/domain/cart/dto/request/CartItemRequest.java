package com.example.ililbooks.domain.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import static com.example.ililbooks.global.dto.ValidationMessage.*;

public record CartItemRequest(@NotNull(message = NOT_NULL_BOOK_ID) Long bookId,
                              @Min(value = 1, message = INVALID_BOOK_QUANTITY) int quantity) {

}
