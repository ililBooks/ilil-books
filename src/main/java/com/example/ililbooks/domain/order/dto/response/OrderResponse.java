package com.example.ililbooks.domain.order.dto.response;

import com.example.ililbooks.domain.order.entity.Order;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record OrderResponse(

        Long orderId,
        String number,
        String orderName,
        Page<OrderHistoryResponse> orderHistoryResponsePage,
        BigDecimal totalPrice,
        String orderStatus,
        String deliveryStatus,
        String paymentStatus,
        String limitedType,
        Instant createdAt,
        Instant modifiedAt
) {

    @Builder
    public OrderResponse {
    }

    public static OrderResponse of(Order order, Page<OrderHistoryResponse> orderHistoryResponsePage) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .number(order.getNumber())
                .orderName(order.getName())
                .orderHistoryResponsePage(orderHistoryResponsePage)
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus().name())
                .deliveryStatus(order.getDeliveryStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .limitedType(order.getLimitedType().name())
                .createdAt(order.getCreatedAt())
                .modifiedAt(order.getModifiedAt())
                .build();
    }
}
