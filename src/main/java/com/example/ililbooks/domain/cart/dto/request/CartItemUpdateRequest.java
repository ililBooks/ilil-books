package com.example.ililbooks.domain.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

import static com.example.ililbooks.global.dto.ValidationMessage.NOT_EMPTY_SHOPPING_CART_ITEM;

@Schema(description = "장바구니에 담은 책들의 수량을 일괄 업데이트하기 위한 요청 DTO")
public record CartItemUpdateRequest(

        @Schema(
                description = "업데이트할 장바구니 항목 리스트",
                example = "[{\"bookId\": 1, \"quantity\": 3}, {\"bookId\": 2, \"quantity\": 5}]"
        )
        @NotEmpty(message = NOT_EMPTY_SHOPPING_CART_ITEM)
        List<CartItemRequest> cartItemList
) {

}
