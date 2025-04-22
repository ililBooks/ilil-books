package com.example.ililbooks.domain.cart.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.enums.LimitedType;
import com.example.ililbooks.domain.cart.dto.response.CartResponse;
import com.example.ililbooks.domain.cart.entity.Cart;
import com.example.ililbooks.domain.cart.entity.CartItem;
import com.example.ililbooks.domain.cart.repository.CartRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.global.dto.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    private AuthUser authUser;
    private Book book;

    @BeforeEach
    void setUp() {
        authUser = AuthUser.builder()
                .userId(1L)
                .email("email@email.com")
                .nickname("nickname")
                .role(UserRole.ROLE_USER)
                .build();

        book = Book.builder()
                .id(1L)
                .title("book1")
                .users(Users.builder().id(2L).userRole(UserRole.ROLE_ADMIN).build())
                .stock(100)
                .price(new BigDecimal(20000))
                .publisher("publisher1")
                .limitedType(LimitedType.REGULAR)
                .build();
    }

    /* getCart */
    @Test
    void 장바구니_조회_성공() {
        // given
        int quantity = 4;

        Cart cart = new Cart(authUser.getUserId());
        cart.getItems().put(book.getId(), CartItem.of(book, quantity));

        given(cartRepository.get(anyLong())).willReturn(cart);

        // when
        CartResponse result = cartService.getCart(authUser);

        // then
        assertEquals(1, result.items().size());
        assertEquals(book.getId(), result.items().get(0).bookId());
        assertEquals(quantity, result.items().get(0).quantity());
    }

    /* clearCart */
    @Test
    void 장바구니_비우기_성공() {
        // given

        // when
        cartService.clearCart(authUser);

        // then
        verify(cartRepository, times(1)).clear(anyLong());
    }

    /* findByUserIdOrElseNewCart */
    @Test
    void 장바구니_조회_성공시_기존_장바구니_반환() {
        // given
        Long userId = 1L;
        Cart cart = new Cart(authUser.getUserId());

        given(cartRepository.get(anyLong())).willReturn(cart);

        // when
        Cart result = cartService.findByUserIdOrElseNewCart(userId);

        // then
        assertSame(cart, result);
        verify(cartRepository, times(1)).get(userId);
    }

    /* findByUserIdOrElseNewCart */
    @Test
    void 장바구니_조회_실패시_새_장바구니_반환() {
        // given
        Long userId = 1L;
        Cart cart = new Cart(authUser.getUserId());

        given(cartRepository.get(anyLong())).willReturn(null);

        // when
        Cart result = cartService.findByUserIdOrElseNewCart(userId);

        // then
        assertTrue(result.getItems().isEmpty());
        verify(cartRepository, times(1)).get(userId);
    }
}