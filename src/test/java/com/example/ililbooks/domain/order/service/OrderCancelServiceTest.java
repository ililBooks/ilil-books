package com.example.ililbooks.domain.order.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.service.BookStockService;
import com.example.ililbooks.domain.cart.entity.Cart;
import com.example.ililbooks.domain.cart.entity.CartItem;
import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.enums.DeliveryStatus;
import com.example.ililbooks.domain.order.enums.LimitedType;
import com.example.ililbooks.domain.order.enums.OrderStatus;
import com.example.ililbooks.domain.order.enums.PaymentStatus;
import com.example.ililbooks.domain.payment.enums.PayStatus;
import com.example.ililbooks.domain.payment.service.PaymentService;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.payment.entity.Payment;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.ForbiddenException;
import com.siot.IamportRestClient.exception.IamportResponseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.example.ililbooks.global.exception.ErrorMessage.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderCancelServiceTest {

    @Mock
    private OrderService orderService;
    @Mock
    private OrderHistoryService orderHistoryService;
    @Mock
    private BookStockService bookStockService;
    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderCancelService orderCancelService;

    private AuthUser authUser;
    private Users adminUsers;
    private final Pageable pageable = PageRequest.of(0, 10);
    private Book book1, book2;
    private Order order;
    private OrderResponse orderResponse;
    private Payment paidPayment, pendingPayment;

    @BeforeEach
    void setUp() {
        authUser = AuthUser.builder()
                .userId(1L)
                .email("email@email.com")
                .nickname("nickname")
                .role(UserRole.ROLE_USER)
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

        order = Order.builder()
                .id(1L)
                .users(Users.fromAuthUser(authUser))
                .number("order-number")
                .limitedType(LimitedType.REGULAR)
                .build();

        orderResponse = OrderResponse.builder()
                .number("order-number")
                .orderStatus(OrderStatus.CANCELLED.name())
                .deliveryStatus(DeliveryStatus.IN_TRANSIT.name())
                .build();

        paidPayment = Payment.builder()
                .id(1L)
                .payStatus(PayStatus.PAID)
                .build();

        pendingPayment = Payment.builder()
                .id(1L)
                .payStatus(PayStatus.READY)
                .build();
    }

    /* cancelOrder */
    /* --- 실패 케이스 --- */
    @Test
    void 주문_취소_해당_유저의_주문이_아니라_실패() {
        // given
        Long orderId = 1L;
        Order notAuthUserOrder = Order.builder()
                .id(1L)
                .users(Users.builder().id(2L).build())
                .build();

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(notAuthUserOrder);

        // when & then
        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
                () -> orderCancelService.cancelOrder(authUser, orderId, pageable));
        assertEquals(forbiddenException.getMessage(), NOT_OWN_ORDER.getMessage());
    }

    @Test
    void 주문_취소_해당_주문의_상태가_취소_상태라_살패() {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.CANCELLED);
        ReflectionTestUtils.setField(order, "paymentStatus", PaymentStatus.FAILED);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.READY);
        ReflectionTestUtils.setField(order, "limitedType", LimitedType.REGULAR);

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(order);

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> orderCancelService.cancelOrder(authUser, orderId, pageable));
        assertEquals(badRequestException.getMessage(), CANNOT_CANCEL_ORDER.getMessage());
    }

    @Test
    void 주문_취소_한정판이라서_실패() {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.ORDERED);
        ReflectionTestUtils.setField(order, "paymentStatus", PaymentStatus.FAILED);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.READY);
        ReflectionTestUtils.setField(order, "limitedType", LimitedType.LIMITED);    // false

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(order);

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> orderCancelService.cancelOrder(authUser, orderId, pageable));
        assertEquals(CANNOT_CANCEL_ORDER.getMessage(), exception.getMessage());
    }

    @Test
    void 주문_취소_주문상태때문에_실패() {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.COMPLETE);   // false
        ReflectionTestUtils.setField(order, "paymentStatus", PaymentStatus.FAILED);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.READY);
        ReflectionTestUtils.setField(order, "limitedType", LimitedType.REGULAR);

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(order);

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> orderCancelService.cancelOrder(authUser, orderId, pageable));
        assertEquals(CANNOT_CANCEL_ORDER.getMessage(), exception.getMessage());
    }

    @Test
    void 주문_취소_배송상태때문에_실패() {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.ORDERED);
        ReflectionTestUtils.setField(order, "paymentStatus", PaymentStatus.PAID);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.IN_TRANSIT);   // false
        ReflectionTestUtils.setField(order, "limitedType", LimitedType.REGULAR);

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(order);

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> orderCancelService.cancelOrder(authUser, orderId, pageable));
        assertEquals(CANNOT_CANCEL_ORDER.getMessage(), exception.getMessage());
    }

    @Test
    void 주문_취소_결제상태때문에_실패() {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.ORDERED);
        ReflectionTestUtils.setField(order, "paymentStatus", PaymentStatus.CANCELLED); // false
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.READY);
        ReflectionTestUtils.setField(order, "limitedType", LimitedType.REGULAR);

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(order);

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> orderCancelService.cancelOrder(authUser, orderId, pageable));
        assertEquals(CANNOT_CANCEL_ORDER.getMessage(), exception.getMessage());
    }

    /* --- 성공 케이스 --- */
    @Test
    void 주문_취소_결제_상태가_아닐_경우_성공() throws IamportResponseException, IOException {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.PENDING);
        ReflectionTestUtils.setField(order, "paymentStatus", PaymentStatus.PENDING);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.READY);
        ReflectionTestUtils.setField(order, "limitedType", LimitedType.REGULAR);

        int book1originalQuantity = 2;
        int book2originalQuantity = 3;
        List<CartItem> cartItemList = Arrays.asList(
                CartItem.of(book1, book1originalQuantity),
                CartItem.of(book2, book2originalQuantity)
        );

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(order);
        given(orderHistoryService.getCartItemListByOrderId(anyLong())).willReturn(cartItemList);
        given(paymentService.getTopByOrderIdOrderByCreatedAtDesc(anyLong())).willReturn(Optional.empty());
        given(orderService.getOrderResponse(any(Order.class), any(Pageable.class))).willReturn(orderResponse);

        // When
        OrderResponse result = orderCancelService.cancelOrder(authUser, orderId, pageable);

        // Then
        assertEquals(OrderStatus.CANCELLED.name(), result.orderStatus());
        verify(bookStockService, times(2)).rollbackStock(anyLong(), anyInt());
    }

    @Test
    void 주문_취소_결제_관련_이력이_있을_경우_성공() throws IamportResponseException, IOException {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.PENDING);
        ReflectionTestUtils.setField(order, "paymentStatus", PaymentStatus.PENDING);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.READY);
        ReflectionTestUtils.setField(order, "limitedType", LimitedType.REGULAR);

        int book1originalQuantity = 2;
        int book2originalQuantity = 3;
        List<CartItem> cartItemList = Arrays.asList(
                CartItem.of(book1, book1originalQuantity),
                CartItem.of(book2, book2originalQuantity)
        );

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(order);
        given(orderHistoryService.getCartItemListByOrderId(anyLong())).willReturn(cartItemList);
        given(paymentService.getTopByOrderIdOrderByCreatedAtDesc(anyLong())).willReturn(Optional.of(pendingPayment));
        given(orderService.getOrderResponse(any(Order.class), any(Pageable.class))).willReturn(orderResponse);

        // When
        OrderResponse result = orderCancelService.cancelOrder(authUser, orderId, pageable);

        // Then
        assertEquals(OrderStatus.CANCELLED.name(), result.orderStatus());
        verify(bookStockService, times(2)).rollbackStock(anyLong(), anyInt());
    }

    @Test
    void 주문_취소_결제_완료_이력이_있을_경우_성공() throws IamportResponseException, IOException {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.PENDING);
        ReflectionTestUtils.setField(order, "paymentStatus", PaymentStatus.PENDING);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.READY);
        ReflectionTestUtils.setField(order, "limitedType", LimitedType.REGULAR);

        int book1originalQuantity = 2;
        int book2originalQuantity = 3;
        List<CartItem> cartItemList = Arrays.asList(
                CartItem.of(book1, book1originalQuantity),
                CartItem.of(book2, book2originalQuantity)
        );

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(order);
        given(orderHistoryService.getCartItemListByOrderId(anyLong())).willReturn(cartItemList);
        given(paymentService.getTopByOrderIdOrderByCreatedAtDesc(anyLong())).willReturn(Optional.of(paidPayment));
        given(orderService.getOrderResponse(any(Order.class), any(Pageable.class))).willReturn(orderResponse);

        // When
        OrderResponse result = orderCancelService.cancelOrder(authUser, orderId, pageable);

        // Then
        assertEquals(OrderStatus.CANCELLED.name(), result.orderStatus());
        verify(bookStockService, times(2)).rollbackStock(anyLong(), anyInt());
        verify(paymentService, times(1)).cancelPayment(any(AuthUser.class), anyLong());
    }
}