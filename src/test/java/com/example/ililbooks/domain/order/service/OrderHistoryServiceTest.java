package com.example.ililbooks.domain.order.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.enums.LimitedType;
import com.example.ililbooks.domain.cart.entity.Cart;
import com.example.ililbooks.domain.cart.entity.CartItem;
import com.example.ililbooks.domain.order.dto.response.OrderHistoryResponse;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.entity.OrderHistory;
import com.example.ililbooks.domain.order.repository.OrderHistoryRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderHistoryServiceTest {

    @Mock
    private OrderHistoryRepository orderHistoryRepository;

    @InjectMocks
    private OrderHistoryService orderHistoryService;

    private final Pageable pageable = PageRequest.of(0, 10);
    private Order order;
    private Book book1, book2;
    private Cart cart;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .id(1L)
                .build();

        book1 = Book.builder()
                .id(1L)
                .title("book1")
                .users(Users.builder().id(2L).userRole(UserRole.ROLE_ADMIN).build())
                .stock(100)
                .price(new BigDecimal(20000))
                .publisher("publisher1")
                .limitedType(LimitedType.REGULAR)
                .build();

        book2 = Book.builder()
                .id(2L)
                .title("book2")
                .users(Users.builder().id(2L).userRole(UserRole.ROLE_ADMIN).build())
                .stock(200)
                .price(new BigDecimal(30000))
                .publisher("publisher2")
                .limitedType(LimitedType.REGULAR)
                .build();

        cart = Cart.builder()
                .items(new HashMap<>(){{
                    put(1L, CartItem.of(100L, 2));
                    put(2L, CartItem.of(200L, 3));
                }})
                .build();
    }

    @Test
    void 주문_이력_저장_성공() {
        // Given
        Map<Long, Book> bookMap = new HashMap<>();
        bookMap.put(1L, book1);
        bookMap.put(2L, book2);

        // When
        orderHistoryService.saveOrderHistory(bookMap, cart, order);

        // Then
        verify(orderHistoryRepository, times(2)).save(any(OrderHistory.class));
    }

    @Test
    void 주문_내역_조회_페이징_성공() {
        // Given
        List<OrderHistory> histories = List.of(
                OrderHistory.of(order, book1, 2),
                OrderHistory.of(order, book2, 3)
        );
        Page<OrderHistory> page = new PageImpl<>(histories);

        given(orderHistoryRepository.findAllByOrderId(anyLong(), any(Pageable.class))).willReturn(page);

        // When
        Page<OrderHistoryResponse> result = orderHistoryService.getOrderHistories(1L, pageable);

        // Then
        assertEquals(page.getContent().get(0).getTitle(), result.getContent().get(0).title());
        assertEquals(page.getContent().get(1).getTitle(), result.getContent().get(1).title());
        verify(orderHistoryRepository).findAllByOrderId(1L, pageable);
    }

    @Test
    void 주문_내역_책_다건_조회_성공() {
        // Given
        Long orderId = 1L;
        List<OrderHistory> histories = List.of(
                OrderHistory.of(order, book1, 2),
                OrderHistory.of(order, book2, 3)
        );

        given(orderHistoryRepository.findAllByOrderId(orderId)).willReturn(histories);

        // When
        List<CartItem> result = orderHistoryService.getCartItemListByOrderId(orderId);

        // Then
        assertEquals(histories.get(0).getBook().getId(), result.get(0).getBookId());
        assertEquals(histories.get(1).getBook().getId(), result.get(1).getBookId());
        verify(orderHistoryRepository).findAllByOrderId(anyLong());
    }

    @Test
    void 주문_내역_책_다건_조회_빈배열_성공() {
        // Given
        given(orderHistoryRepository.findAllByOrderId(anyLong())).willReturn(Collections.emptyList());

        // When
        List<CartItem> result = orderHistoryService.getCartItemListByOrderId(1L);

        // Then
        assertTrue(result.isEmpty());
    }
}