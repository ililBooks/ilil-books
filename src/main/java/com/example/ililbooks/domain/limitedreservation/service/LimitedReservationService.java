package com.example.ililbooks.domain.limitedreservation.service;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedevent.repository.LimitedEventRepository;
import com.example.ililbooks.domain.limitedreservation.dto.request.LimitedReservationCreateRequest;
import com.example.ililbooks.domain.limitedreservation.dto.response.LimitedReservationResponse;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import com.example.ililbooks.domain.limitedreservation.repository.LimitedReservationRepository;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.service.OrderService;
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
    private final OrderService orderService;

    private static final int EXPIRATION_HOURS = 24;

    /*
     * 예약 생성
     */
    @Transactional
    public LimitedReservationResponse createReservation(AuthUser authUser, LimitedReservationCreateRequest request) {
        Users user = findUser(authUser.getUserId());
        LimitedEvent limitedEvent = findEvent(request.limitedEventId());

        // 중복 예약 체크
        limitedReservationRepository.findByUsersAndLimitedEvent(user, limitedEvent).ifPresent(reservation -> {
            throw new BadRequestException(ALREADY_RESERVED_EVENT.getMessage());
        });

        // 예약 수량 체크
        long successCount = limitedReservationRepository.countByLimitedEventAndStatus(limitedEvent, LimitedReservationStatus.SUCCESS);
        boolean isReservable = limitedEvent.canAcceptReservation(successCount);

        LimitedReservationStatus status = isReservable ? LimitedReservationStatus.SUCCESS : LimitedReservationStatus.WAITING;
        Instant expiredAt = Instant.now().plus(EXPIRATION_HOURS, ChronoUnit.HOURS);

        // 주문 생성 및 예약 연결 (if-success)
        LimitedReservation reservation;
        if (status == LimitedReservationStatus.SUCCESS) {
            Order order = orderService.createOrderForReservation(user);
            reservation = LimitedReservation.createWithOrder(user, limitedEvent, status, expiredAt, order);
        } else {
            reservation = LimitedReservation.of(user, limitedEvent, status, expiredAt);
        }

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

    // --- 내부 메서드 ---
    private Users findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_USER.getMessage())
        );
    }

    private LimitedEvent findEvent(Long eventId) {
        return limitedEventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_EVENT.getMessage())
        );
    }

    private LimitedReservation findReservation(Long reservationId) {
        return limitedReservationRepository.findById(reservationId).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_RESERVATION.getMessage())
        );
    }
}
