package com.example.ililbooks.domain.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import static com.example.ililbooks.global.dto.ValidationMessage.*;

@Schema(description = "장바구니에 담은 책과 수량을 담는 위한 DTO")
public record CartItemRequest(

        @Schema(example = "1")
        @NotNull(message = NOT_NULL_BOOK_ID)
        Long bookId,

        @Schema(example = "3")
        @Min(value = 1, message = INVALID_BOOK_QUANTITY)
        int quantity
) {
        @Builder
        public CartItemRequest {
        }
}
