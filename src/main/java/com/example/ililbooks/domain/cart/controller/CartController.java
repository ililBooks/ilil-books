package com.example.ililbooks.domain.cart.controller;

import com.example.ililbooks.domain.cart.dto.request.CartItemUpdateRequest;
import com.example.ililbooks.domain.cart.dto.response.CartResponse;
import com.example.ililbooks.domain.cart.service.CartService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.USER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;

    /* 장바구니 추가 및 삭제 */
    @Secured(USER)
    @PostMapping
    public Response<CartResponse> updateCart(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CartItemUpdateRequest cartItemUpdateRequest
    ) {
        return Response.of(cartService.updateCart(authUser, cartItemUpdateRequest));
    }

    /* 장바구니 조회 */
    @Secured(USER)
    @GetMapping
    public Response<CartResponse> getCart(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return Response.of(cartService.getCart(authUser));
    }

    /* 장바구니 비우기 */
    @DeleteMapping
    public Response<Void> clearCart(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        cartService.clearCart(authUser);
        return Response.empty();
    }
}
