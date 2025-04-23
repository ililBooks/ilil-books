package com.example.ililbooks.domain.order.service;

import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.dto.response.OrdersGetResponse;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.enums.DeliveryStatus;
import com.example.ililbooks.domain.order.enums.LimitedType;
import com.example.ililbooks.domain.order.enums.OrderStatus;
import com.example.ililbooks.domain.order.enums.PaymentStatus;
import com.example.ililbooks.domain.order.repository.OrderRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;

import static com.example.ililbooks.global.exception.ErrorMessage.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderReadServiceTest {

    @Mock
    private OrderService orderService;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderReadService orderReadService;

    private AuthUser authUser;
    private final Pageable pageable = PageRequest.of(0, 10);
    private Order order1, order2;
    private OrderResponse orderResponse;


    @BeforeEach
    void setUp() {
        authUser = AuthUser.builder()
                .userId(1L)
                .email("email@email.com")
                .nickname("nickname")
                .role(UserRole.ROLE_USER)
                .build();

        order1 = Order.builder()
                .id(1L)
                .users(Users.fromAuthUser(authUser))
                .number("order1-number")
                .orderStatus(OrderStatus.ORDERED)
                .deliveryStatus(DeliveryStatus.READY)
                .paymentStatus(PaymentStatus.PAID)
                .totalPrice(new BigDecimal("50000"))
                .limitedType(LimitedType.REGULAR)
                .build();

        order2 = Order.builder()
                .id(2L)
                .users(Users.fromAuthUser(authUser))
                .number("order2-number")
                .orderStatus(OrderStatus.COMPLETE)
                .deliveryStatus(DeliveryStatus.DELIVERED)
                .paymentStatus(PaymentStatus.PAID)
                .totalPrice(new BigDecimal("75000"))
                .limitedType(LimitedType.REGULAR)
                .build();

        orderResponse = OrderResponse.builder()
                .number("order-number")
                .build();
    }

    /* findOrder */
    @Test
    void 주문_단건_조회_본인_주문이_아니라_실패() {
        // given
        Long orderId = 1L;
        Order notAuthUserOrder = Order.builder()
                .id(1L)
                .users(Users.builder().id(2L).build())
                .build();

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(notAuthUserOrder);

        // when & then
        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
                () -> orderReadService.findOrder(authUser, orderId, pageable));
        assertEquals(forbiddenException.getMessage(), NOT_OWN_ORDER.getMessage());
    }

    @Test
    void 주문_단건_조회_성공() {
        // given
        Long orderId = 1L;

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(order1);
        given(orderService.getOrderResponse(any(Order.class), any(Pageable.class))).willReturn(orderResponse);

        // when
        OrderResponse result = orderReadService.findOrder(authUser, orderId, pageable);

        // then
        assertEquals(orderResponse.number(), result.number());
    }

    @Test
    void 주문_다건_조회_성공() {
        // given
        List<Order> orders = List.of(order1, order2);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        given(orderRepository.findAllByUsersId(anyLong(), any(Pageable.class))).willReturn(orderPage);

        // When
        Page<OrdersGetResponse> result = orderReadService.getOrders(authUser, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertEquals(orderPage.getNumber(), result.getNumber());

        verify(orderRepository, times(1)).findAllByUsersId(anyLong(), any(Pageable.class));
    }
}