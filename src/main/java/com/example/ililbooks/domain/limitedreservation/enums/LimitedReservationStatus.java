package com.example.ililbooks.domain.limitedreservation.enums;

public enum LimitedReservationStatus {
    SUCCESS,    // 결제 및 예약 성공
    RESERVED,   // 결제 및 예약 성공 대기
    WAITING,    // 대기 상태
    CANCELED    // 취소
}
