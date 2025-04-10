package com.example.ililbooks.domain.cart.service;

import com.example.ililbooks.domain.book.service.BookService;
import com.example.ililbooks.domain.cart.dto.request.CartItemUpdateRequest;
import com.example.ililbooks.domain.cart.dto.request.CartItemRequest;
import com.example.ililbooks.domain.cart.dto.response.CartItemResponse;
import com.example.ililbooks.domain.cart.dto.response.CartResponse;
import com.example.ililbooks.domain.cart.entity.Cart;
import com.example.ililbooks.domain.cart.entity.CartItem;
import com.example.ililbooks.domain.cart.repository.CartRepository;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.ililbooks.global.exception.ErrorMessage.CANNOT_ADD_BOOK_TO_CART;
import static com.example.ililbooks.global.exception.ErrorMessage.CART_QUANTITY_INVALID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final BookService bookService;

    /* 장바구니 추가 및 삭제 */
    public CartResponse updateCart(AuthUser authUser, CartItemUpdateRequest cartItemUpdateRequest) {

        Cart cart = findByUserIdOrElseNewCart(authUser.getUserId());

        for (CartItemRequest item : cartItemUpdateRequest.cartItemList()) {

            if (!bookService.existsOnSaleRegularBookById(item.bookId())) {
                throw new BadRequestException(CANNOT_ADD_BOOK_TO_CART.getMessage());
            }

            CartItem existingItem = cart.getItems().get(item.bookId());

            if (existingItem != null) {
                int updatedQuantity = existingItem.getQuantity() + item.quantity();

                if (updatedQuantity < 0) {
                    throw new BadRequestException(CART_QUANTITY_INVALID.getMessage());
                }

                if (updatedQuantity == 0) {
                    cart.getItems().remove(item.bookId());
                }

                existingItem.updateQuantity(item.quantity());
            } else {
                if (item.quantity() <= 0) {
                    throw new BadRequestException(CART_QUANTITY_INVALID.getMessage());
                }
                cart.getItems().put(item.bookId(), CartItem.of(item.bookId(), item.quantity()));
            }
        }

        cartRepository.put(authUser.getUserId(), cart);
        return getCartResponse(authUser, cart);
    }

    /* 장바구니 조회 */
    public CartResponse getCart(AuthUser authUser) {
        Cart cart = findByUserIdOrElseNewCart(authUser.getUserId());
        return getCartResponse(authUser, cart);
    }

    /* 장바구니 비우기 */
    public void clearCart(AuthUser authUser) {
        cartRepository.clear(authUser.getUserId());
    }

    /* 장바구니 조회 및 생성 */
    public Cart findByUserIdOrElseNewCart(Long userId) {
        return Optional.ofNullable(cartRepository.get(userId))
                .orElse(new Cart(userId));
    }

    /* dto 변환 */
    private CartResponse getCartResponse(AuthUser authUser, Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().values().stream()
                .map(CartItemResponse::of)
                .collect(Collectors.toList());
        //빈배열 -> 에러 안던져줌
        return CartResponse.of(authUser.getUserId(), itemResponses);
    }
}