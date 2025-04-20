package com.example.ililbooks.domain.limitedreservation.entity;

import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import com.example.ililbooks.global.entity.TimeStamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "limited_reservation_status_history")
public class LimitedReservationStatusHistory extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reservationId;

    @Enumerated(EnumType.STRING)
    private LimitedReservationStatus fromStatus;

    @Enumerated(EnumType.STRING)
    private LimitedReservationStatus toStatus;

    public static LimitedReservationStatusHistory of(Long reservationId, LimitedReservationStatus fromStatus, LimitedReservationStatus toStatus) {
        LimitedReservationStatusHistory history = new LimitedReservationStatusHistory();
        history.reservationId = reservationId;
        history.fromStatus = fromStatus;
        history.toStatus = toStatus;
        return history;
    }
}
