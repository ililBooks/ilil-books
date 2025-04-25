package com.example.ililbooks.domain.order.enums;

public enum PaymentStatus {
    PENDING,        // 결제 대기
    PAID,           // 결제 완료
    FAILED,         // 결제 실패
    CANCELLED       // 결제 취소
}
