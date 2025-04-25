package com.example.ililbooks.domain.order.dto.response;

import com.example.ililbooks.domain.order.entity.Order;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record OrdersGetResponse(

        Long orderId,
        String number,
        String orderName,
        BigDecimal totalPrice,
        String orderStatus,
        String deliveryStatus,
        String paymentStatus,
        String limitedType,
        Instant createdAt
) {

    @Builder
    public OrdersGetResponse {
    }

    public static OrdersGetResponse of(Order order) {
        return OrdersGetResponse.builder()
                .orderId(order.getId())
                .number(order.getNumber())
                .orderName(order.getName())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus().name())
                .deliveryStatus(order.getDeliveryStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .limitedType(order.getLimitedType().name())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
