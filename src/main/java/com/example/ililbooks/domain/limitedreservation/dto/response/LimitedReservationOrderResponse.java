package com.example.ililbooks.domain.limitedreservation.dto.response;

import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservationOrder;
import com.example.ililbooks.domain.order.enums.DeliveryStatus;
import com.example.ililbooks.domain.order.enums.OrderStatus;
import com.example.ililbooks.domain.order.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LimitedReservationOrderResponse(
        Long reservationOrderId,
        Long reservationId,
        Long bookId,
        BigDecimal totalPrice,
        OrderStatus orderStatus,
        DeliveryStatus deliveryStatus,
        PaymentStatus paymentStatus,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {

    public static LimitedReservationOrderResponse from(LimitedReservationOrder order) {
        return new LimitedReservationOrderResponse(
                order.getId(),
                order.getReservation().getId(),
                order.getBook().getId(),
                order.getTotalPrice(),
                order.getOrderStatus(),
                order.getDeliveryStatus(),
                order.getPaymentStatus(),
                order.getCreatedAt(),
                order.getModifiedAt()
        );
    }
}