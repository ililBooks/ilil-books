package com.example.ililbooks.domain.order.service;

import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.enums.DeliveryStatus;
import com.example.ililbooks.domain.order.enums.OrderStatus;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import static com.example.ililbooks.global.exception.ErrorMessage.CANNOT_DELIVER_CANCELLED_ORDER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderDeliveryServiceTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderDeliveryService orderDeliveryService;

    private AuthUser authUser;
    private final Pageable pageable = PageRequest.of(0, 10);
    private Order order;
    private OrderResponse inTransitOrderResponse;
    private OrderResponse completeOrderResponse;

    @BeforeEach
    void setUp() {
        authUser = AuthUser.builder()
                .userId(1L)
                .email("email@email.com")
                .nickname("nickname")
                .role(UserRole.ROLE_USER)
                .build();

        order = Order.builder()
                .id(1L)
                .users(Users.fromAuthUser(authUser))
                .number("order-number")
                .build();

        inTransitOrderResponse = OrderResponse.builder()
                .number("order-number")
                .orderStatus(OrderStatus.ORDERED.name())
                .deliveryStatus(DeliveryStatus.IN_TRANSIT.name())
                .build();

        completeOrderResponse = OrderResponse.builder()
                .number("order-number")
                .orderStatus(OrderStatus.COMPLETE.name())
                .deliveryStatus(DeliveryStatus.DELIVERED.name())
                .build();
    }

    /* updateDeliveryStatus */
    /* --- 실패 케이스 --- */
    @Test
    void 주문_배송_변경_주문_취소_상태라_실패() {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.CANCELLED);

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(order);

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> orderDeliveryService.updateDeliveryStatus(orderId, pageable));
        assertEquals(badRequestException.getMessage(), CANNOT_DELIVER_CANCELLED_ORDER.getMessage());
    }

    /* --- 성공 케이스 --- */
    @Test
    void 주문_배송_변경_배송_대기에서_배송중_상태로_변경_성공() {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.ORDERED);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.READY);

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(order);
        given(orderService.getOrderResponse(any(Order.class), any(Pageable.class))).willReturn(inTransitOrderResponse);

        // when
        OrderResponse result = orderDeliveryService.updateDeliveryStatus(orderId, pageable);

        // then
        assertEquals(DeliveryStatus.IN_TRANSIT.name(), result.deliveryStatus());
        verify(orderService, times(1)).getOrderResponse(order, pageable);
    }

    @Test
    void 주문_배송_변경_배송중에서_배송_완료로_변경_성공() {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.ORDERED);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.IN_TRANSIT);

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(order);
        given(orderService.getOrderResponse(any(Order.class), any(Pageable.class))).willReturn(completeOrderResponse);

        // when
        OrderResponse result = orderDeliveryService.updateDeliveryStatus(orderId, pageable);

        // then
        assertEquals(DeliveryStatus.DELIVERED.name(), result.deliveryStatus());
        assertEquals(OrderStatus.COMPLETE.name(), result.orderStatus());
        verify(orderService, times(1)).getOrderResponse(order, pageable);
    }
}