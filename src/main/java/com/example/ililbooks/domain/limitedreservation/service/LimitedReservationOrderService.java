package com.example.ililbooks.domain.limitedreservation.service;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import com.example.ililbooks.domain.limitedreservation.repository.LimitedReservationRepository;
import com.example.ililbooks.domain.order.dto.response.OrdersGetResponse;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class LimitedReservationOrderService {

    private final LimitedReservationRepository limitedReservationRepository;
    private final UserService userService;

    /*
     * 성공 예약 기반으로 주문 생성
     */
    @Transactional
    public OrdersGetResponse createOrderFromReservation(AuthUser authUser, Long reservationId) {
        LimitedReservation reservation = validateReservation(authUser, reservationId);
        LimitedEvent limitedEvent = reservation.getLimitedEvent();

        // 예약 유효시간(결제) 초과 여부 확인
        if (reservation.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException(RESERVATION_EXPIRED.getMessage());
        }

        // 중복 주문여부 확인
        if (reservation.hasOrder()) {
            throw new BadRequestException(ALREADY_ORDERED.getMessage());
        }

        // 주문 생성 및 예약 연결
        Users user = userService.findByIdOrElseThrow(authUser.getUserId());
        Order order = Order.of(user, limitedEvent.getBook().getPrice());
        reservation.linkOrder(order);

        return OrdersGetResponse.of(order);
    }

    /*
     * 유효성 검증 메서드
     */
    private LimitedReservation validateReservation(AuthUser authUser, Long reservationId) {
        LimitedReservation reservation = limitedReservationRepository.findById(reservationId).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_RESERVATION.getMessage())
        );

        if (!reservation.getUsers().getId().equals(authUser.getUserId())) {
            throw new BadRequestException(NO_PERMISSION.getMessage());
        }

        if (reservation.getStatus() != LimitedReservationStatus.SUCCESS) {
            throw new BadRequestException(RESERVATION_NOT_SUCCESS.getMessage());
        }

        return reservation;
    }
}
