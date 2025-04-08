package com.example.ililbooks.domain.cart.service;

import com.example.ililbooks.domain.cart.dto.request.CartItemUpdateRequest;
import com.example.ililbooks.domain.cart.dto.request.CartItemRequest;
import com.example.ililbooks.domain.cart.dto.response.CartItemResponse;
import com.example.ililbooks.domain.cart.dto.response.CartResponse;
import com.example.ililbooks.domain.cart.entity.Cart;
import com.example.ililbooks.domain.cart.entity.CartItem;
import com.example.ililbooks.domain.cart.repository.CartRepository;
import com.example.ililbooks.global.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    /* 장바구니 추가 및 삭제 */
    public CartResponse updateCart(AuthUser authUser, CartItemUpdateRequest cartItemUpdateRequest) {

        Cart cart = cartRepository.get(authUser.getUserId());

        if (cart == null) {
            cart = new Cart(authUser.getUserId());
        }

        for (CartItemRequest item : cartItemUpdateRequest.getCartItemList()) {
            Optional<CartItem> cartItem = Optional.ofNullable(cart.getItems().get(item.getBookId()));

            if (cartItem.isPresent()) {
                cartItem.get().updateQuantity(item.getQuantity());
            } else {
                cart.getItems().put(item.getBookId(), new CartItem(item.getBookId(), item.getQuantity()));
            }
        }
        cartRepository.put(authUser.getUserId(), cart);
        return getCartResponse(authUser, cart);
    }

    /* 장바구니 조회 */
    public CartResponse getCart(AuthUser authUser) {
        Cart cart = cartRepository.get(authUser.getUserId());
        return getCartResponse(authUser, cart);
    }

    /* dto 변환 */
    private CartResponse getCartResponse(AuthUser authUser, Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().values().stream()
                .map(CartItemResponse::of)
                .collect(Collectors.toList());

        return CartResponse.of(authUser.getUserId(), itemResponses);
    }
}