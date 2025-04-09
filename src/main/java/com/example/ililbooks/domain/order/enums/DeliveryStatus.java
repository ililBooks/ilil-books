package com.example.ililbooks.domain.order.enums;

import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.global.exception.BadRequestException;

import static com.example.ililbooks.global.exception.ErrorMessage.CANNOT_START_DELIVERY;
import static com.example.ililbooks.global.exception.ErrorMessage.COMPLETE_DELIVERY;

public enum DeliveryStatus {
    READY,              // 배송 준비
    IN_TRANSIT,         // 배송 중
    DELIVERED;          // 배송 완료

    public boolean isCanCancel() {
        return this == READY;
    }

    public DeliveryStatus nextDeliveryStatus(Order order){
        return switch (order.getDeliveryStatus()) {
            case READY -> {
                if (!order.getOrderStatus().isCanDelivery()) {
                    throw new BadRequestException(CANNOT_START_DELIVERY.getMessage());
                }
                yield IN_TRANSIT;
            }
            case IN_TRANSIT -> DELIVERED;
            case DELIVERED -> throw new BadRequestException(COMPLETE_DELIVERY.getMessage());
        };
    }
}