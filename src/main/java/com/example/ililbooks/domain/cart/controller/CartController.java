package com.example.ililbooks.domain.cart.controller;

import com.example.ililbooks.domain.cart.dto.request.CartItemAddRequest;
import com.example.ililbooks.domain.cart.dto.request.CartItemRequest;
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

    @Secured(USER)
    @PostMapping
    public Response<CartResponse> addCart(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CartItemAddRequest cartItemAddRequest
    ) {
        return Response.of(cartService.addCart(authUser, cartItemAddRequest));
    }

    @GetMapping
    public Response<CartResponse> getCart(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return Response.of(cartService.getCart(authUser));
    }
}
