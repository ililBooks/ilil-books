package com.example.ililbooks.domain.order.dto.response;

import com.example.ililbooks.domain.order.entity.Order;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class OrderResponse {

    private final String number;

    private final Page<OrderHistoryResponse> orderHistoryResponsePage;

    private final BigDecimal totalPrice;

    private final String orderStatus;

    private final String deliveryStatus;

    private final String paymentStatus;

    private final LocalDateTime createdAt;

    private final LocalDateTime modifiedAt;

    @Builder
    public OrderResponse(String number, Page<OrderHistoryResponse> orderHistoryResponsePage, BigDecimal totalPrice, String orderStatus, String deliveryStatus, String paymentStatus, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.number = number;
        this.orderHistoryResponsePage = orderHistoryResponsePage;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.deliveryStatus = deliveryStatus;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
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
