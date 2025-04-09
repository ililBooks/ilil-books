package com.example.ililbooks.domain.order.dto.response;

import com.example.ililbooks.domain.order.entity.Order;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//
//@Getter
//public record OrderResponse(Long id, Long userId, String number, BigDecimal totalPrice, String orderStatus,
//                            String deliveryStatus, String paymentStatus, LocalDateTime createdAt,
//                            LocalDateTime modifiedAt) {
//
//    @Builder
//    public OrderResponse {
//    }
//
//    public static OrderResponse of(Order order) {
//        return OrderResponse.builder()
//                .id(order.getId())
//                .userId(order.getUsers().getId())
//                .number(order.getNumber())
//                .totalPrice(order.getTotalPrice())
//                .orderStatus(order.getOrderStatus().name())
//                .deliveryStatus(order.getDeliveryStatus().name())
//                .paymentStatus(order.getPaymentStatus().name())
//                .createdAt(order.getCreatedAt())
//                .modifiedAt(order.getModifiedAt())
//                .build();
//    }
//}


@Getter
public class OrderResponse {

    private final Long id;

    private final Long userId;

    private final String number;

    private final BigDecimal totalPrice;

    private final String orderStatus;

    private final String deliveryStatus;

    private final String paymentStatus;

    private final LocalDateTime createdAt;

    private final LocalDateTime modifiedAt;

    @Builder
    public OrderResponse(Long id, Long userId, String number, BigDecimal totalPrice, String orderStatus, String deliveryStatus, String paymentStatus, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.id = id;
        this.userId = userId;
        this.number = number;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.deliveryStatus = deliveryStatus;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public static OrderResponse of(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUsers().getId())
                .number(order.getNumber())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus().name())
                .deliveryStatus(order.getDeliveryStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .createdAt(order.getCreatedAt())
                .modifiedAt(order.getModifiedAt())
                .build();
    }
}
