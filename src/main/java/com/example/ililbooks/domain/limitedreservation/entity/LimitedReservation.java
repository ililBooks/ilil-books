package com.example.ililbooks.domain.limitedreservation.entity;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.global.entity.TimeStamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    /*
     * 정적 생성 메서드
     */
    public static LimitedReservation create(Users users, LimitedEvent limitedEvent, LimitedReservationStatus status) {
        LimitedReservation reservation = new LimitedReservation();
        reservation.users = users;
        reservation.limitedEvent = limitedEvent;
        reservation.status = status;
        return reservation;
    }

    /*
     * 예약 취소
     */
    public void cancel() {
        this.status = LimitedReservationStatus.CANCELED;
    }
}
