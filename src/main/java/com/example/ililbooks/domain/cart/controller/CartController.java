package com.example.ililbooks.domain.cart.controller;

import com.example.ililbooks.domain.cart.dto.request.CartItemUpdateRequest;
import com.example.ililbooks.domain.cart.dto.response.CartResponse;
import com.example.ililbooks.domain.cart.service.CartService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.USER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/carts")
@Tag(name = "Cart", description = "장바구니 관련 API")
public class CartController {

    private final CartService cartService;

    /* 장바구니 추가 및 삭제 */
    @Operation(summary = "장바구니 수량 업데이트", description = "장바구니에 담은 책들의 수량을 일괄 업데이트할 수 있습니다.")
    @Secured(USER)
    @PostMapping
    public Response<CartResponse> updateCart(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CartItemUpdateRequest cartItemUpdateRequest
    ) {
        return Response.of(cartService.updateCart(authUser, cartItemUpdateRequest));
    }

    /* 장바구니 조회 */
    @Operation(summary = "장바구니 조회", description = "사용자가 담은 장바구니 내용을 조회할 수 있습니다.")
    @Secured(USER)
    @GetMapping
    public Response<CartResponse> getCart(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return Response.of(cartService.getCart(authUser));
    }

    /* 장바구니 비우기 */
    @Operation(summary = "장바구니 비우기", description = "장바구니에 담은 내용을 비울 수 있습니다.")
    @Secured(USER)
    @DeleteMapping
    public Response<Void> clearCart(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        cartService.clearCart(authUser);
        return Response.empty();
    }
}
