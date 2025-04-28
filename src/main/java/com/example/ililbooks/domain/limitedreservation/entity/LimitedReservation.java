package com.example.ililbooks.domain.limitedreservation.entity;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.global.entity.TimeStamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "limited_reservation", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "limited_event_id"})
})
public class LimitedReservation extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "limited_event_id", nullable = false)
    private LimitedEvent limitedEvent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LimitedReservationStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Builder
    private LimitedReservation(Long id, Users users, LimitedEvent limitedEvent, LimitedReservationStatus status, Instant expiresAt, Order order) {
        this.id = id;
        this.users = users;
        this.limitedEvent = limitedEvent;
        this.status = status;
        this.expiresAt = expiresAt;
        this.order = order;
    }

    /*
     * 정적 생성 메서드
     */
    public static LimitedReservation of(Users users, LimitedEvent limitedEvent, LimitedReservationStatus status, Instant expiresAt) {
        return LimitedReservation.builder()
                .users(users)
                .limitedEvent(limitedEvent)
                .status(status)
                .expiresAt(expiresAt)
                .build();
    }

    /*
     * 정적 생성 메서드 (주문 포함)
     */
    public static LimitedReservation createWithOrder(Users users, LimitedEvent limitedEvent, LimitedReservationStatus status, Instant expiresAt, Order order) {
        return LimitedReservation.builder()
                .users(users)
                .limitedEvent(limitedEvent)
                .status(status)
                .expiresAt(expiresAt)
                .order(order)
                .build();
    }

    /*
     * 예약 상태를 취소로 변경
     */
    public void markCanceled() {
        this.status = LimitedReservationStatus.CANCELED;
    }

    /*
     * 예약에 주문 연결
     */
    public void linkOrder(Order order) {
        this.order = order;
    }

    /*
     * 주문 연동 여부 확인
     */
    public boolean hasOrder() {
        return this.order != null;
    }

    public void markSuccess() {
        this.status =LimitedReservationStatus.RESERVED;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }

    public void updateLimitedReservationStatus(LimitedReservationStatus status) {
        this.status = status;
    }
}
