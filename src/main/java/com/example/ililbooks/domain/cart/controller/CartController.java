package com.example.ililbooks.domain.cart.controller;

import com.example.ililbooks.domain.cart.dto.request.CartItemAddRequest;
import com.example.ililbooks.domain.cart.dto.request.CartItemRequest;
import com.example.ililbooks.domain.cart.dto.response.CartResponse;
import com.example.ililbooks.domain.cart.service.CartService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;

    @PostMapping
    public Response<CartResponse> addCart(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CartItemAddRequest cartItemAddRequest
            ) {
        return Response.of(cartService.addCart(authUser, cartItemAddRequest));
    }
}
