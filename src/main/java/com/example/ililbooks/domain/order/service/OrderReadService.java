package com.example.ililbooks.domain.order.service;

import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.dto.response.OrdersGetResponse;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.repository.OrderRepository;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.ililbooks.global.exception.ErrorMessage.NOT_OWN_ORDER;

@Service
@RequiredArgsConstructor
public class OrderReadService {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    /* 주문 단건 조회 */
    @Transactional(readOnly = true)
    public OrderResponse findOrder(AuthUser authUser, Long orderId, Pageable pageable) {
        Order order = orderService.findByIdOrElseThrow(orderId);

        if (!authUser.getUserId().equals(order.getUsers().getId())) {
            throw new ForbiddenException(NOT_OWN_ORDER.getMessage());
        }
        return orderService.getOrderResponse(order, pageable);
    }

    /* 주문 다건 조회 */
    @Transactional(readOnly = true)
    public Page<OrdersGetResponse> getOrders(AuthUser authUser, Pageable pageable) {
        Page<Order> findOrders = orderRepository.findAllByUsersId(authUser.getUserId(), pageable);

        return findOrders.map(OrdersGetResponse::of);
    }
}
