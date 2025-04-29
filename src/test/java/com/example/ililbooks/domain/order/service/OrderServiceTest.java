package com.example.ililbooks.domain.order.service;

import com.example.ililbooks.domain.bestseller.service.BestSellerService;
import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.service.BookStockService;
import com.example.ililbooks.domain.cart.entity.Cart;
import com.example.ililbooks.domain.cart.entity.CartItem;
import com.example.ililbooks.domain.cart.service.CartService;
import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.enums.LimitedType;
import com.example.ililbooks.domain.order.repository.OrderRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.asynchronous.rabbitmq.dto.request.MessageOrderRequest;
import com.example.ililbooks.global.asynchronous.rabbitmq.service.RabbitMqService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Optional;

import static com.example.ililbooks.domain.book.enums.LimitedType.REGULAR;
import static com.example.ililbooks.global.exception.ErrorMessage.NOT_EXIST_SHOPPING_CART;
import static com.example.ililbooks.global.exception.ErrorMessage.NOT_FOUND_ORDER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartService cartService;
    @Mock
    private BookStockService bookStockService;
    @Mock
    private BestSellerService bestSellerService;
    @Mock
    private RabbitMqService rabbitMqService;
    @Mock
    private UserService userService;
    @Mock
    private OrderHistoryService orderHistoryService;

    @InjectMocks
    private OrderService orderService;

    private AuthUser authUser;
    private Users users;
    private Users adminUsers;
    private final Pageable pageable = PageRequest.of(0, 10);
    private Book book1, book2;
    private Cart cart;
    private Order order;

    @BeforeEach
    void setUp() {
        authUser = AuthUser.builder()
                .userId(1L)
                .email("email@email.com")
                .nickname("nickname")
                .role(UserRole.ROLE_USER)
                .build();

        users = Users.builder()
                .email("email@email.com")
                .nickname("nickname")
                .userRole(UserRole.ROLE_USER)
                .isNotificationAgreed(false)
                .build();

        adminUsers = Users.builder()
                .email("admin@email.com")
                .nickname("adminNickname")
                .userRole(UserRole.ROLE_ADMIN)
                .isNotificationAgreed(false)
                .build();

        book1 = Book.builder()
                .id(1L)
                .title("book1")
                .users(adminUsers)
                .stock(100)
                .price(new BigDecimal(20000))
                .publisher("publisher1")
                .limitedType(REGULAR)
                .build();

        book2 = Book.builder()
                .id(2L)
                .title("book2")
                .users(adminUsers)
                .stock(200)
                .price(new BigDecimal(30000))
                .publisher("publisher2")
                .limitedType(REGULAR)
                .build();

        cart = Cart.builder()
                .userId(authUser.getUserId())
                .items(new HashMap<>())
                .build();

        order = Order.builder()
                .id(1L)
                .users(Users.fromAuthUser(authUser))
                .number("order-number")
                .limitedType(LimitedType.REGULAR)
                .build();
    }

    /* createOrder */
    @Test
    void 주문_생성_장바구니가_비어있어_실패() {
        // given
        given(cartService.findByUserIdOrElseNewCart(anyLong())).willReturn(cart);

        // when & then
        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> orderService.createOrder(authUser, pageable));
        assertEquals(notFoundException.getMessage(), NOT_EXIST_SHOPPING_CART.getMessage());
    }

    @Test
    void 주문_생성_알림_수신_비동의_성공() {
        // Given
        int book1originalQuantity = 2;
        int book2originalQuantity = 3;

        cart.getItems().put(book1.getId(), CartItem.of(book1, book1originalQuantity));
        cart.getItems().put(book2.getId(), CartItem.of(book2, book2originalQuantity));

        given(cartService.findByUserIdOrElseNewCart(anyLong())).willReturn(cart);
        willDoNothing().given(bestSellerService).increaseBookSalesByQuantity(anyMap());
        given(userService.findByIdOrElseThrow(anyLong())).willReturn(users);

        // When
        OrderResponse result = orderService.createOrder(authUser, pageable);

        // Then
        assertThat(result.totalPrice()).isEqualTo(new BigDecimal("130000")); // (20000*2 + 30000*3)
        verify(bookStockService, times(2)).decreaseStock(anyLong(), anyInt());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartService, times(1)).clearCart(authUser);
    }

    @Test
    void 주문_생성_알림_수신_동의_성공() {
        // Given
        int book1originalQuantity = 2;
        int book2originalQuantity = 3;
        ReflectionTestUtils.setField(users, "isNotificationAgreed", true);

        cart.getItems().put(book1.getId(), CartItem.of(book1, book1originalQuantity));
        cart.getItems().put(book2.getId(), CartItem.of(book2, book2originalQuantity));

        given(cartService.findByUserIdOrElseNewCart(anyLong())).willReturn(cart);
        willDoNothing().given(bestSellerService).increaseBookSalesByQuantity(anyMap());
        given(userService.findByIdOrElseThrow(anyLong())).willReturn(users);

        // When
        OrderResponse result = orderService.createOrder(authUser, pageable);

        // Then
        assertThat(result.totalPrice()).isEqualTo(new BigDecimal("130000")); // (20000*2 + 30000*3)
        verify(bookStockService, times(2)).decreaseStock(anyLong(), anyInt());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartService, times(1)).clearCart(authUser);
        verify(rabbitMqService, times(1)).sendOrderMessage(any(MessageOrderRequest.class));
    }

    @Test
    void 주문_생성_알림_수신_동의_및_주문_이름_30_초과_성공() {
        // Given
        int book1originalQuantity = 2;
        int book2originalQuantity = 3;
        ReflectionTestUtils.setField(users, "isNotificationAgreed", true);
        ReflectionTestUtils.setField(book1, "title", "book-title-over-30-word-abcdefghijkimnopqrstu");

        cart.getItems().put(book1.getId(), CartItem.of(book1, book1originalQuantity));
        cart.getItems().put(book2.getId(), CartItem.of(book2, book2originalQuantity));

        given(cartService.findByUserIdOrElseNewCart(anyLong())).willReturn(cart);
        willDoNothing().given(bestSellerService).increaseBookSalesByQuantity(anyMap());
        given(userService.findByIdOrElseThrow(anyLong())).willReturn(users);

        // When
        OrderResponse result = orderService.createOrder(authUser, pageable);

        // Then
        assertThat(result.totalPrice()).isEqualTo(new BigDecimal("130000")); // (20000*2 + 30000*3)
        verify(bookStockService, times(2)).decreaseStock(anyLong(), anyInt());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartService, times(1)).clearCart(authUser);
        verify(rabbitMqService, times(1)).sendOrderMessage(any(MessageOrderRequest.class));
    }

    /* findByIdOrElseThrow */
    @Test
    void 주문_조회_실패() {
        // given
        Long orderId = 1L;

        given(orderRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> orderService.findByIdOrElseThrow(orderId));
        assertEquals(notFoundException.getMessage(), NOT_FOUND_ORDER.getMessage());
    }

    @Test
    void 주문_조회_성공() {
        // given
        Long orderId = 1L;

        given(orderRepository.findById(anyLong())).willReturn(Optional.of(order));

        // when
        Order result = orderService.findByIdOrElseThrow(orderId);

        // then
        assertEquals(order.getId(), result.getId());
        assertEquals(order.getNumber(), result.getNumber());
    }
}