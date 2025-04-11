package com.example.ililbooks.domain.limitedreservation.entity;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.global.entity.TimeStamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private Instant expiredAt;

    /*
     * 정적 생성 메서드
     */
    public static LimitedReservation createFrom(Users users, LimitedEvent limitedEvent, LimitedReservationStatus status, Instant expiredAt) {
        LimitedReservation reservation = new LimitedReservation();
        reservation.users = users;
        reservation.limitedEvent = limitedEvent;
        reservation.status = status;
        reservation.expiredAt = expiredAt;
        return reservation;
    }

    /*
     * 예약 상태를 취소로 변경
     */
    public void markCanceled() {
        this.status = LimitedReservationStatus.CANCELED;
    }
}
