package com.example.ililbooks.domain.order.enums;

public enum OrderStatus {
    PENDING,        // 주문 대기
    ORDERED,        // 주문 승인
    COMPLETE,       // 주문 과정 전체 완료
    CANCELLED;      // 주문 취소(유저가 상태변경)

    public boolean isCanCancel() {
        return this == PENDING || this == ORDERED;
    }
}
