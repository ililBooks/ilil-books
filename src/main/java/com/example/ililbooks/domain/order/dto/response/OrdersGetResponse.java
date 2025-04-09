package com.example.ililbooks.domain.order.dto.response;

import com.example.ililbooks.domain.order.entity.Order;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class OrdersGetResponse {

    private final Long orderId;

    private final String number;

    private final BigDecimal totalPrice;

    private final String orderStatus;

    private final String deliveryStatus;

    private final String paymentStatus;

    private final LocalDateTime createdAt;

    @Builder
    public OrdersGetResponse(Long orderId, String number, BigDecimal totalPrice, String orderStatus, String deliveryStatus, String paymentStatus, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.number = number;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.deliveryStatus = deliveryStatus;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
    }

    public static OrdersGetResponse of(Order order) {
        return OrdersGetResponse.builder()
                .orderId(order.getId())
                .number(order.getNumber())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus().name())
                .deliveryStatus(order.getDeliveryStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
