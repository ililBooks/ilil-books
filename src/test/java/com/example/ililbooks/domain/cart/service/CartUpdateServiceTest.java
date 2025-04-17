package com.example.ililbooks.domain.cart.service;

import com.example.ililbooks.domain.book.service.BookService;
import com.example.ililbooks.domain.cart.dto.request.CartItemRequest;
import com.example.ililbooks.domain.cart.dto.request.CartItemUpdateRequest;
import com.example.ililbooks.domain.cart.dto.response.CartResponse;
import com.example.ililbooks.domain.cart.entity.Cart;
import com.example.ililbooks.domain.cart.entity.CartItem;
import com.example.ililbooks.domain.cart.repository.CartRepository;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;

import static com.example.ililbooks.global.exception.ErrorMessage.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CartUpdateServiceTest {

    @Mock
    private BookService bookService;
    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    private AuthUser authUser;

    @BeforeEach
    void setUp() {
        authUser = AuthUser.builder()
                .userId(1L)
                .email("email@email.com")
                .nickname("nickname")
                .role(UserRole.ROLE_USER)
                .build();
    }

    /* --- 실패 테스트 케이스 --- */

    @Test
    void updateCart_판매_안하는_책_추가시_예외_실패() {
        // given
        given(bookService.existsOnSaleRegularBookById(anyLong())).willReturn(false);

        CartItemRequest item = new CartItemRequest(1L, 2); // 수량은 양수(id 1책 수량 2권 추가)
        CartItemUpdateRequest request = new CartItemUpdateRequest(List.of(item));

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> cartService.updateCart(authUser, request));
        assertEquals(badRequestException.getMessage(), CANNOT_ADD_BOOK_TO_CART.getMessage());
    }

    @Test
    void updateCart_수량_음수_예외_실패() {
        // given
        Long bookId = 1L;
        Cart cart = new Cart(authUser.getUserId(), new HashMap<>());
        cart.getItems().put(bookId, CartItem.of(bookId, 1)); // 장바구니에 담긴 1책 1권

        given(bookService.existsOnSaleRegularBookById(anyLong())).willReturn(true);
        given(cartRepository.get(anyLong())).willReturn(cart);

        CartItemRequest item = new CartItemRequest(bookId, -2); // 수량 줄이면 -1 됨
        CartItemUpdateRequest request = new CartItemUpdateRequest(List.of(item));

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> cartService.updateCart(authUser, request));
        assertEquals(badRequestException.getMessage(), CART_QUANTITY_INVALID.getMessage());
    }

    @Test
    void updateCart_수량_0인_새책추가_예외() {
        // given
        Cart cart = new Cart(authUser.getUserId(), new HashMap<>());

        given(bookService.existsOnSaleRegularBookById(anyLong())).willReturn(true);
        given(cartRepository.get(anyLong())).willReturn(cart);

        CartItemRequest item = new CartItemRequest(1L, 0); // 수량 0
        CartItemUpdateRequest request = new CartItemUpdateRequest(List.of(item));

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> cartService.updateCart(authUser, request));
        assertEquals(badRequestException.getMessage(), CART_QUANTITY_INVALID.getMessage());
    }

    /* --- 성공 테스트 케이스 ---*/
    @Test
    void 기존_아이템_수량_증가_성공() {
        // given
        Long bookId = 1L;
        int originalQuantity = 1;
        int increaseBy = 2;

        Cart cart = new Cart(authUser.getUserId());
        cart.getItems().put(bookId, CartItem.of(bookId, originalQuantity));

        given(bookService.existsOnSaleRegularBookById(anyLong())).willReturn(true);
        given(cartRepository.get(anyLong())).willReturn(cart);

        CartItemRequest item = new CartItemRequest(1L, increaseBy); // 수량 0
        CartItemUpdateRequest request = new CartItemUpdateRequest(List.of(item));

        // when
        CartResponse result = cartService.updateCart(authUser, request);

        // then
        assertEquals(originalQuantity + increaseBy, result.items().get(0).quantity());
    }

    @Test
    void 기존_아이템_수량_감소_성공() {
        // given
        Long bookId = 1L;
        int originalQuantity = 2;
        int decreaseBy = -1;

        Cart cart = new Cart(authUser.getUserId());
        cart.getItems().put(bookId, CartItem.of(bookId, originalQuantity));

        given(bookService.existsOnSaleRegularBookById(anyLong())).willReturn(true);
        given(cartRepository.get(anyLong())).willReturn(cart);

        CartItemRequest item = new CartItemRequest(1L, decreaseBy);
        CartItemUpdateRequest request = new CartItemUpdateRequest(List.of(item));

        // when
        CartResponse result = cartService.updateCart(authUser, request);

        // then
        assertEquals(originalQuantity + decreaseBy, result.items().get(0).quantity());
    }

    @Test
    void 기존_아이템_수량_0으로_감소시_삭제_성공() {
        // given
        Long bookId = 1L;
        int originalQuantity = 1;
        int decreaseBy = -1;

        Cart cart = new Cart(authUser.getUserId());
        cart.getItems().put(bookId, CartItem.of(bookId, originalQuantity));

        given(bookService.existsOnSaleRegularBookById(anyLong())).willReturn(true);
        given(cartRepository.get(anyLong())).willReturn(cart);

        CartItemRequest item = new CartItemRequest(1L, decreaseBy);
        CartItemUpdateRequest request = new CartItemUpdateRequest(List.of(item));

        // when
        CartResponse result = cartService.updateCart(authUser, request);

        // then
        assertTrue(result.items().isEmpty()); // 해당 책이 장바구니에서 사라짐
    }

    @Test
    void 새_책_추가_성공() {
        // given
        Long bookId = 4L;
        int quantity = 3;

        Cart cart = new Cart(authUser.getUserId());

        given(bookService.existsOnSaleRegularBookById(bookId)).willReturn(true);
        given(cartRepository.get(anyLong())).willReturn(cart);

        CartItemUpdateRequest request = new CartItemUpdateRequest(
                List.of(new CartItemRequest(bookId, quantity))
        );

        // when
        CartResponse result = cartService.updateCart(authUser, request);

        // then
        assertEquals(1, result.items().size());
        assertEquals(bookId, result.items().get(0).bookId());
        assertEquals(quantity, result.items().get(0).quantity());
    }

    @Test
    void 장바구니가_없으면_새로_생성됨() {
        // given
        Long bookId = 10L;
        int quantity = 2;

        given(cartRepository.get(anyLong())).willReturn(new Cart(authUser.getUserId()));
        given(bookService.existsOnSaleRegularBookById(bookId)).willReturn(true);

        CartItemUpdateRequest request = new CartItemUpdateRequest(
                List.of(new CartItemRequest(bookId, quantity))
        );

        // when
        CartResponse result = cartService.updateCart(authUser, request);

        // then
        assertEquals(1, result.items().size());
        assertEquals(bookId, result.items().get(0).bookId());
        assertEquals(quantity, result.items().get(0).quantity());
    }

    @Test
    void 여러_책을_한번에_처리() {
        // given
        Long bookId1 = 20L;
        Long bookId2 = 21L;

        Cart cart = new Cart(authUser.getUserId());

        given(cartRepository.get(anyLong())).willReturn(cart);
        given(bookService.existsOnSaleRegularBookById(bookId1)).willReturn(true);
        given(bookService.existsOnSaleRegularBookById(bookId2)).willReturn(true);

        CartItemUpdateRequest request = new CartItemUpdateRequest(
                List.of(
                        new CartItemRequest(bookId1, 3),
                        new CartItemRequest(bookId2, 5)
                )
        );

        // when
        CartResponse result = cartService.updateCart(authUser, request);

        // then
        assertEquals(2, result.items().size());
        assertTrue(result.items().stream().anyMatch(item -> item.bookId().equals(bookId1) && item.quantity() == 3));
        assertTrue(result.items().stream().anyMatch(item -> item.bookId().equals(bookId2) && item.quantity() == 5));
    }
}