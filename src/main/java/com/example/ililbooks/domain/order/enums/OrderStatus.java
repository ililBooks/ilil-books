package com.example.ililbooks.domain.order.enums;

public enum OrderStatus {
    PENDING,        // 주문 대기
    ORDERED,        // 주문 완료
    CANCELLED       // 주문 취소(유저가 상태변경)
}
