package com.example.ililbooks.domain.order.service;

import com.example.ililbooks.domain.bestseller.service.BestSellerService;
import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.service.BookService;
import com.example.ililbooks.domain.book.service.BookStockService;
import com.example.ililbooks.domain.cart.entity.Cart;
import com.example.ililbooks.domain.cart.entity.CartItem;
import com.example.ililbooks.domain.cart.service.CartService;
import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.enums.DeliveryStatus;
import com.example.ililbooks.domain.order.enums.LimitedType;
import com.example.ililbooks.domain.order.enums.OrderStatus;
import com.example.ililbooks.domain.order.enums.PaymentStatus;
import com.example.ililbooks.domain.order.repository.OrderRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.ForbiddenException;
import com.example.ililbooks.global.exception.NotFoundException;
import org.apache.catalina.User;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.example.ililbooks.global.exception.ErrorMessage.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartService cartService;
    @Mock
    private OrderHistoryService orderHistoryService;
    @Mock
    private BookStockService bookStockService;
    @Mock
    private BestSellerService bestSellerService;
    @Mock
    private UserService userService;


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
                .build();

        book2 = Book.builder()
                .id(2L)
                .title("book2")
                .users(adminUsers)
                .stock(200)
                .price(new BigDecimal(30000))
                .publisher("publisher2")
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
    void 주문_생성_성공() {
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
        verify(orderRepository).save(any(Order.class));
        verify(cartService).clearCart(authUser);
    }

    /* cancelOrder */
    @Test
    void 주문_취소_주문이_없어_실패() {
        // given
        Long orderId = 100L;

        given(orderRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> orderService.cancelOrder(authUser, orderId, pageable));
        assertEquals(notFoundException.getMessage(), NOT_FOUND_ORDER.getMessage());
    }

    @Test
    void 주문_취소_해당_유저의_주문이_아니라_실패() {
        // given
        Long orderId = 1L;
        Order notAuthUserOrder = Order.builder()
                .id(1L)
                .users(Users.builder().id(2L).build())
                .build();

        given(orderRepository.findById(anyLong())).willReturn(Optional.of(notAuthUserOrder));

        // when & then
        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
                () -> orderService.cancelOrder(authUser, orderId, pageable));
        assertEquals(forbiddenException.getMessage(), NOT_OWN_ORDER.getMessage());
    }

    @Test
    void 주문_취소_해당_주문의_상태가_취소_상태라_살패() {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.CANCELLED);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.READY);

        given(orderRepository.findById(anyLong())).willReturn(Optional.of(order));

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> orderService.cancelOrder(authUser, orderId, pageable));
        assertEquals(badRequestException.getMessage(), CANNOT_CANCEL_ORDER.getMessage());
    }

    @Test
    void 주문_취소_해당_주문의_상태가_완료_상태라_실패() {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.COMPLETE);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.READY);

        given(orderRepository.findById(anyLong())).willReturn(Optional.of(order));

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> orderService.cancelOrder(authUser, orderId, pageable));
        assertEquals(badRequestException.getMessage(), CANNOT_CANCEL_ORDER.getMessage());
    }

    @Test
    void 주문_취소_해당_주문의_상태가_배송_중_상태라_실패() {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.PENDING);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.IN_TRANSIT);

        given(orderRepository.findById(anyLong())).willReturn(Optional.of(order));

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> orderService.cancelOrder(authUser, orderId, pageable));
        assertEquals(badRequestException.getMessage(), CANNOT_CANCEL_ORDER.getMessage());
    }

    @Test
    void 주문_취소_해당_주문의_상태가_배송_완료_상태라_실패() {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.PENDING);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.DELIVERED);

        given(orderRepository.findById(anyLong())).willReturn(Optional.of(order));

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> orderService.cancelOrder(authUser, orderId, pageable));
        assertEquals(badRequestException.getMessage(), CANNOT_CANCEL_ORDER.getMessage());
    }

    @Test
    void 주문_취소_성공() {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.PENDING);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.READY);
        ReflectionTestUtils.setField(order, "paymentStatus", PaymentStatus.PAID);

        int book1originalQuantity = 2;
        int book2originalQuantity = 3;
        List<CartItem> cartItemList = Arrays.asList(
                CartItem.of(book1, book1originalQuantity),
                CartItem.of(book2, book2originalQuantity)
        );

        given(orderRepository.findById(anyLong())).willReturn(Optional.of(order));
        given(orderHistoryService.getCartItemListByOrderId(orderId)).willReturn(cartItemList);

        // When
        OrderResponse result = orderService.cancelOrder(authUser, orderId, pageable);

        // Then
        assertEquals(OrderStatus.CANCELLED.name(), result.orderStatus());
        verify(bookStockService, times(2)).rollbackStock(anyLong(), anyInt());
        verify(orderHistoryService, times(1)).getOrderHistories(orderId, pageable);
    }

    @Test
    void 주문_승인_주문이_없어_실패() {
        // given
        Long orderId = 100L;

        given(orderRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> orderService.updateOrderStatus(authUser, orderId, pageable));
        assertEquals(notFoundException.getMessage(), NOT_FOUND_ORDER.getMessage());
    }

    @Test
    void 주문_승인_해당_유저의_주문이_아니라_실패() {
        // given
        Long orderId = 1L;
        Order notAuthUserOrder = Order.builder()
                .id(1L)
                .users(Users.builder().id(2L).build())
                .build();

        given(orderRepository.findById(anyLong())).willReturn(Optional.of(notAuthUserOrder));

        // when & then
        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
                () -> orderService.updateOrderStatus(authUser, orderId, pageable));
        assertEquals(forbiddenException.getMessage(), NOT_OWN_ORDER.getMessage());
    }

    @Test
    void 주문_승인_해당_주문의_상태가_대기상태가_아니라_살패() {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.CANCELLED);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.READY);
        ReflectionTestUtils.setField(order, "paymentStatus", PaymentStatus.PENDING);

        given(orderRepository.findById(anyLong())).willReturn(Optional.of(order));

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> orderService.updateOrderStatus(authUser, orderId, pageable));
        assertEquals(badRequestException.getMessage(), CANNOT_CHANGE_ORDER.getMessage());
    }

    @Test
    void 주문_승인_성공() {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.PENDING);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.READY);
        ReflectionTestUtils.setField(order, "paymentStatus", PaymentStatus.PAID);

        given(orderRepository.findById(anyLong())).willReturn(Optional.of(order));

        // When
        OrderResponse result = orderService.updateOrderStatus(authUser, orderId, pageable);

        // Then
        assertEquals(OrderStatus.ORDERED.name(), result.orderStatus());
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