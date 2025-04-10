package com.example.ililbooks.domain.order.dto.response;

import com.example.ililbooks.domain.order.entity.Order;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(String number, Page<OrderHistoryResponse> orderHistoryResponsePage, BigDecimal totalPrice,
                            String orderStatus, String deliveryStatus, String paymentStatus, LocalDateTime createdAt,
                            LocalDateTime modifiedAt) {

    @Builder
    public OrderResponse {
    }

    public static OrderResponse of(Order order, Page<OrderHistoryResponse> orderHistoryResponsePage) {
        return OrderResponse.builder()
                .number(order.getNumber())
                .orderHistoryResponsePage(orderHistoryResponsePage)
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus().name())
                .deliveryStatus(order.getDeliveryStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .createdAt(order.getCreatedAt())
                .modifiedAt(order.getModifiedAt())
                .build();
    }
}
