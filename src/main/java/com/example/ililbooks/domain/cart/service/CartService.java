package com.example.ililbooks.domain.cart.service;

import com.example.ililbooks.domain.cart.dto.request.CartItemAddRequest;
import com.example.ililbooks.domain.cart.dto.request.CartItemRequest;
import com.example.ililbooks.domain.cart.dto.response.CartItemResponse;
import com.example.ililbooks.domain.cart.dto.response.CartResponse;
import com.example.ililbooks.domain.cart.entity.Cart;
import com.example.ililbooks.domain.cart.entity.CartItem;
import com.example.ililbooks.global.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartService {

    private final RedisClient redisClient;

    public CartResponse addCart(AuthUser authUser, CartItemAddRequest cartItemAddRequest) {

        Cart cart = redisClient.get(authUser.getUserId(), Cart.class);

        if (cart == null) {
            cart = new Cart(authUser.getUserId());
        }

        for (CartItemRequest item : cartItemAddRequest.getCartItemList()) {
            Optional<CartItem> cartItem = Optional.ofNullable(cart.getItems().get(item.getBookId()));

            if (cartItem.isPresent()) {
                cartItem.get().increaseQuantity(item.getQuantity());
            } else {
                cart.getItems().put(item.getBookId(), new CartItem(item.getBookId(), item.getQuantity()));
            }
        }

        redisClient.put(authUser.getUserId(), cart);

        List<CartItemResponse> itemResponses = cart.getItems().values().stream()
                .map(CartItemResponse::of)
                .collect(Collectors.toList());

        return CartResponse.of(authUser.getUserId(), itemResponses);
    }
}