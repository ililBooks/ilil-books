package com.example.ililbooks.domain.limitedreservation.service;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedevent.repository.LimitedEventRepository;
import com.example.ililbooks.domain.limitedreservation.dto.request.LimitedReservationCreateRequest;
import com.example.ililbooks.domain.limitedreservation.dto.response.LimitedReservationResponse;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservationStatusHistory;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import com.example.ililbooks.domain.limitedreservation.repository.LimitedReservationRepository;
import com.example.ililbooks.domain.limitedreservation.repository.LimitedReservationStatusHistoryRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.global.redis.lock.RedissonLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class LimitedReservationService {

    private final LimitedReservationRepository reservationRepository;
    private final LimitedEventRepository eventRepository;
    private final UserService userService;
    private final LimitedReservationQueueService queueService;
    private final LimitedReservationExpireQueueService expireQueueService;
    private final RedissonLockService redissonLockService;
    private final LimitedReservationStatusHistoryRepository historyRepository;

    private static final int EXPIRATION_HOURS = 24;

    /*/ 예약 생성 */
    @Transactional
    public LimitedReservationResponse createReservation(AuthUser authUser,  LimitedReservationCreateRequest request) {
        String lockKey=  "lock:event" + request.limitedEventId();

        // Redisson 분산 락 적용
        return redissonLockService.runWithLock(lockKey,() -> createReservationWithLock(authUser, request));
    }

    public LimitedReservationResponse createReservationWithLock(AuthUser authUser, LimitedReservationCreateRequest request) {
        LimitedEvent event = findEvent(request.limitedEventId());
        validateNotAlreadyReserved(authUser.getUserId(), event);

        Users user = userService.findByIdOrElseThrow(authUser.getUserId());
        Instant expiresAt = Instant.now().plus(EXPIRATION_HOURS, ChronoUnit.HOURS);

        long successCount = reservationRepository.countByLimitedEventAndStatus(event, LimitedReservationStatus.SUCCESS);
        boolean isReservable = event.canAcceptReservation(successCount);

        LimitedReservation reservation = isReservable ?
                LimitedReservation.of(user, event, LimitedReservationStatus.SUCCESS, expiresAt) :
                LimitedReservation.of(user, event, LimitedReservationStatus.WAITING, expiresAt);

        reservationRepository.save(reservation);

        if (isReservable) {
            event.decreaseBookQuantity(1);
            expireQueueService.addToExpireQueue(event.getId(), reservation.getId(), expiresAt);
        } else {
            queueService.enqueue(event.getId(), reservation.getId(), Instant.now());
        }

        historyRepository.save(LimitedReservationStatusHistory.of(reservation.getId(), null, reservation.getStatus()));
        return LimitedReservationResponse.of(reservation);
    }

    /*/ 예약 취소 */
    @Transactional
    public void cancelReservation(AuthUser authUser, Long reservationId) {
        LimitedReservation reservation = findReservation(reservationId);
        if (!reservation.getUsers().getId().equals(authUser.getUserId())) {
            throw new BadRequestException(NOT_OWN_RESERVATION.getMessage());
        }

        LimitedReservationStatus from = reservation.getStatus();
        reservation.markCanceled();

        historyRepository.save(LimitedReservationStatusHistory.of(reservation.getId(), from, reservation.getStatus()));
        queueService.remove(reservation.getLimitedEvent().getId(), reservation.getId());

        promoteNextWaitingReservation(reservation.getLimitedEvent().getId());
    }

    /*/ 예약 만료 전체 처리 */
    @Transactional
    public void expireReservationAndPromote() {
        Instant now = Instant.now();
        List<LimitedReservation> expired = reservationRepository.findAllByStatusAndExpiredAtBefore(LimitedReservationStatus.SUCCESS, now);

        for (LimitedReservation reservation : expired) {
            cancelAndPromote(reservation);
        }
    }

    /*/ 예약 단건 만료 처리 */
    @Transactional
    public void expireReservationAndPromoteOne(Long reservationId) {
        LimitedReservation reservation = findReservation(reservationId);

        if (reservation.getStatus() != LimitedReservationStatus.SUCCESS || !reservation.isExpired()) return;
        cancelAndPromote(reservation);
    }

    // ---- 내부 메서드 ----

    private void cancelAndPromote(LimitedReservation reservation) {
        LimitedReservationStatus from = reservation.getStatus();
        reservation.markCanceled();
        historyRepository.save(LimitedReservationStatusHistory.of(reservation.getId(), from, reservation.getStatus()));
        queueService.remove(reservation.getLimitedEvent().getId(), reservation.getId());
        promoteNextWaitingReservation(reservation.getLimitedEvent().getId());
    }

    private void promoteNextWaitingReservation(Long eventId) {
        Long nextId = queueService.dequeue(eventId);
        if (nextId == null) return;

        LimitedReservation waiting = reservationRepository.findById(nextId).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_RESERVATION.getMessage()));

        LimitedReservationStatus from = waiting.getStatus();
        waiting.markSuccess();
        waiting.getLimitedEvent().decreaseBookQuantity(1);
        historyRepository.save(LimitedReservationStatusHistory.of(waiting.getId(), from, waiting.getStatus()));
    }

    private void validateNotAlreadyReserved(Long userId, LimitedEvent event) {
        reservationRepository.findByUsersIdAndLimitedEvent(userId, event)
                .ifPresent(r -> {
                    throw new BadRequestException(ALREADY_RESERVED_EVENT.getMessage());
                });
    }

    private LimitedEvent findEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_EVENT.getMessage()));
    }

    private LimitedReservation findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_RESERVATION.getMessage()));
    }
}
