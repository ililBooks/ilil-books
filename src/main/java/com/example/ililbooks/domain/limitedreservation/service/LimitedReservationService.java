package com.example.ililbooks.domain.limitedreservation.service;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedevent.repository.LimitedEventRepository;
import com.example.ililbooks.domain.limitedreservation.dto.request.LimitedReservationCreateRequest;
import com.example.ililbooks.domain.limitedreservation.dto.response.LimitedReservationResponse;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import com.example.ililbooks.domain.limitedreservation.repository.LimitedReservationRepository;
import com.example.ililbooks.domain.order.dto.response.OrdersGetResponse;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.repository.UserRepository;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class LimitedReservationService {

    private final LimitedReservationRepository limitedReservationRepository;
    private final LimitedEventRepository limitedEventRepository;
    private final UserRepository userRepository;

    private static final int EXPIRATION_HOURS = 24;

    /*
     * 예약 생성
     */
    @Transactional
    public LimitedReservationResponse createReservation(AuthUser authUser, LimitedReservationCreateRequest request) {
        LimitedEvent limitedEvent = findEvent(request.limitedEventId());
        validateNotAlreadyReserved(authUser.getUserId(), limitedEvent);

        long successCount = limitedReservationRepository.countByLimitedEventAndStatus(limitedEvent, LimitedReservationStatus.SUCCESS);
        boolean isReservable = limitedEvent.canAcceptReservation(successCount);

        LimitedReservationStatus status = isReservable ? LimitedReservationStatus.SUCCESS : LimitedReservationStatus.WAITING;
        Instant expiresAt = Instant.now().plus(EXPIRATION_HOURS, ChronoUnit.HOURS);

        Users user = findUser(authUser.getUserId());

        LimitedReservation reservation = LimitedReservation.of(user, limitedEvent, status, expiresAt);
        limitedReservationRepository.save(reservation);

        return LimitedReservationResponse.of(reservation);
    }

    /*
     * 예약 단건 조회
     */
    @Transactional(readOnly = true)
    public LimitedReservationResponse getReservationByUser(AuthUser authUser, Long reservationId) {
        LimitedReservation reservation = findReservation(reservationId);

        if (!reservation.getUsers().getId().equals(authUser.getUserId())) {
            throw new BadRequestException(NOT_OWN_RESERVATION.getMessage());
        }
        return LimitedReservationResponse.of(reservation);
    }

    /*
     * 예약 전체 조회 (출판사용)
     */
    @Transactional(readOnly = true)
    public Page<LimitedReservationResponse> getReservationsByEvent(Long eventId, Pageable pageable) {
        LimitedEvent limitedEvent = findEvent(eventId);
        return limitedReservationRepository.findAllByLimitedEvent(limitedEvent, pageable)
                .map(LimitedReservationResponse::of);
    }

    /*
     * 예약 상태별 조회 (출판사/관리자) - V2용
     */
//    @Transactional(readOnly = true)
//    public List<LimitedReservationResponse> getReservationsByEventAndStatus(Long eventId, List<LimitedReservationStatus> statuses) {
//        LimitedEvent limitedEvent = findEvent(eventId);
//        List<LimitedReservation> reservations = limitedReservationRepository.findAllByLimitedEventAndStatusIn(limitedEvent, statuses);
//
//        return reservations.stream()
//                .map(LimitedReservationResponse::of)
//                .toList();
//    }

    /*
     * 예약 취소
     */
    @Transactional
    public void cancelReservation(AuthUser authUser, Long reservationId) {
        LimitedReservation reservation = findReservation(reservationId);

        if (!reservation.getUsers().getId().equals(authUser.getUserId())) {
            throw new BadRequestException(NOT_OWN_RESERVATION.getMessage());
        }
        reservation.markCanceled();
    }

    /*
     * 성공한 예약 기반 주문 생성
     */
    @Transactional
    public OrdersGetResponse createOrderForReservation(AuthUser authUser, Long reservationId) {
        LimitedReservation reservation = validateReservation(authUser, reservationId);
        LimitedEvent limitedEvent = reservation.getLimitedEvent();

        if (reservation.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException(RESERVATION_EXPIRED.getMessage());
        }

        if (reservation.hasOrder()) {
            throw new BadRequestException(ALREADY_ORDERED.getMessage());
        }

        if (limitedEvent.getBookQuantity() < 1) {
            throw new BadRequestException(OUT_OF_STOCK.getMessage());
        }

        limitedEvent.decreaseBookQuantity(1);

        Users user = findUser(authUser.getUserId());
        Order order = Order.of(user, limitedEvent.getBook().getPrice());
        reservation.linkOrder(order);

        return OrdersGetResponse.of(order);
    }

    // --- 내부 메서드 ---
    private void validateNotAlreadyReserved(Long userId, LimitedEvent event) {
        limitedReservationRepository.findByUsersIdAndLimitedEvent(userId, event)
                .ifPresent(r -> {
                    throw new BadRequestException(ALREADY_RESERVED_EVENT.getMessage());
                });
    }

    private LimitedReservation validateReservation(AuthUser authUser, Long reservationId) {
        LimitedReservation reservation = findReservation(reservationId);

        if (!reservation.getUsers().getId().equals(authUser.getUserId())) {
            throw new BadRequestException(NO_PERMISSION.getMessage());
        }

        if (reservation.getStatus() != LimitedReservationStatus.SUCCESS) {
            throw new BadRequestException(RESERVATION_NOT_SUCCESS.getMessage());
        }

        return reservation;
    }

    private LimitedEvent findEvent(Long limitedEventId) {
        return limitedEventRepository.findById(limitedEventId).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_EVENT.getMessage())
        );
    }

    private LimitedReservation findReservation(Long reservationId) {
        return limitedReservationRepository.findById(reservationId).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_RESERVATION.getMessage())
        );
    }

    private Users findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_USER.getMessage()));
    }
}
