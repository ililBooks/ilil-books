package com.example.ililbooks.domain.limitedreservation.service;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedreservation.dto.response.LimitedReservationOrderResponse;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservationOrder;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import com.example.ililbooks.domain.limitedreservation.repository.LimitedReservationOrderRepository;
import com.example.ililbooks.domain.limitedreservation.repository.LimitedReservationRepository;
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
    private final LimitedReservationOrderRepository limitedReservationOrderRepository;

    /*
     * 예약을 기반으로 주문 생성
     * - 예약 상태 SUCCESS
     * - 결제 만료 전이어야함
     * - 한정 수량이 최소 1 이상
     */
    @Transactional
    public LimitedReservationOrderResponse createOrderFromReservation(AuthUser authUser, Long reservationId) {
        LimitedReservation reservation = validateReservation(authUser, reservationId);
        LimitedEvent limitedEvent = reservation.getLimitedEvent();

        // 만료된 예약은 주문 불가
        if (reservation.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException(RESERVATION_EXPIRED.getMessage());
        }

        // 이미 주문이 연결되어 있다면 중복 생성 방지
        if (reservation.hasOrder()) {
            throw new BadRequestException(ALREADY_ORDERED.getMessage());
        }

        // 남은 수량 확인 및 차감
        if (limitedEvent.getBookQuantity() < 1) {
            throw new BadRequestException(OUT_OF_STOCK.getMessage());
        }
        limitedEvent.decreaseBookQuantity(1);

        // 주문 생성
        LimitedReservationOrder limitedOrder = LimitedReservationOrder.of(
                reservation,
                limitedEvent.getBook(),
                limitedEvent.getBook().getPrice(),
                1
        );

        limitedReservationOrderRepository.save(limitedOrder);

        return LimitedReservationOrderResponse.of(limitedOrder);
    }

    /*
     * 예약 유효성 검증 - 유저 일치, 상태 SUCCESS
     * - 유저 일치
     * - 상태가 SUCCESS
     */
    private LimitedReservation validateReservation(AuthUser authUser, Long reservationId) {
        LimitedReservation reservation = limitedReservationRepository.findByIdWithEvent(reservationId).orElseThrow(
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