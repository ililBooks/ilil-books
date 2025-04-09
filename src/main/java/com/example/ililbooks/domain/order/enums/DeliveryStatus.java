package com.example.ililbooks.domain.order.enums;

public enum DeliveryStatus {
    READY,              // 배송 준비
    IN_TRANSIT,         // 배송 중
    DELIVERED;          // 배송 완료

    public boolean isCanCancel() {
        return this == READY;
    }
}