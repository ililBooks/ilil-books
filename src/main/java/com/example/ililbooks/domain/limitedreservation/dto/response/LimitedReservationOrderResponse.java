package com.example.ililbooks.domain.limitedreservation.dto.response;

import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservationOrder;
import com.example.ililbooks.domain.order.enums.DeliveryStatus;
import com.example.ililbooks.domain.order.enums.OrderStatus;
import com.example.ililbooks.domain.order.enums.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

public record LimitedReservationOrderResponse(
        Long reservationOrderId,
        Long reservationId,
        Long bookId,
        BigDecimal totalPrice,
        OrderStatus orderStatus,
        DeliveryStatus deliveryStatus,
        PaymentStatus paymentStatus,
        Instant createdAt,
        Instant modifiedAt
) {
    @Builder
    public LimitedReservationOrderResponse {
    }

    public static LimitedReservationOrderResponse of(LimitedReservationOrder order) {
        return LimitedReservationOrderResponse.builder()
                .reservationOrderId(order.getId())
                .reservationId(order.getReservation().getId())
                .bookId(order.getBook().getId())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus())
                .deliveryStatus(order.getDeliveryStatus())
                .paymentStatus(order.getPaymentStatus())
                .createdAt(order.getCreatedAt())
                .modifiedAt(order.getModifiedAt())
                .build();
    }
}