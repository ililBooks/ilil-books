package com.example.ililbooks.domain.order.service;

import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.enums.DeliveryStatus;
import com.example.ililbooks.domain.order.enums.OrderStatus;
import com.example.ililbooks.domain.order.enums.PaymentStatus;
import com.example.ililbooks.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.ililbooks.domain.order.enums.DeliveryStatus.DELIVERED;
import static com.example.ililbooks.global.exception.ErrorMessage.CANNOT_DELIVERY_CANCELLED_ORDER;
import static com.example.ililbooks.global.exception.ErrorMessage.CANNOT_DELIVERY_ORDER;

@Service
@RequiredArgsConstructor
public class OrderDeliveryService {

    private final OrderService orderService;

    /* 배송 상태 변경 (대기 -> 배송중 -> 배송완료) */
    @Transactional
    public OrderResponse updateDeliveryStatus(Long orderId, Pageable pageable) {
        Order order = orderService.findByIdOrElseThrow(orderId);

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException(CANNOT_DELIVERY_CANCELLED_ORDER.getMessage());
        }

        if (order.getOrderStatus() != OrderStatus.ORDERED
                || order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new BadRequestException(CANNOT_DELIVERY_ORDER.getMessage());
        }

        DeliveryStatus deliveryStatus = order.getDeliveryStatus().nextDeliveryStatus(order);
        order.updateDelivery(deliveryStatus);

        if (deliveryStatus == DELIVERED) {
            order.updateOrder(OrderStatus.COMPLETE);
        }
        return orderService.getOrderResponse(order, pageable);
    }
}
