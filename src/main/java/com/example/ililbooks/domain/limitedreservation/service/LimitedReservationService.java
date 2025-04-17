package com.example.ililbooks.domain.limitedreservation.service;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedevent.repository.LimitedEventRepository;
import com.example.ililbooks.domain.limitedreservation.dto.request.LimitedReservationCreateRequest;
import com.example.ililbooks.domain.limitedreservation.dto.response.LimitedReservationResponse;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import com.example.ililbooks.domain.limitedreservation.repository.LimitedReservationRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.global.lock.RedissonLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class LimitedReservationService {

    private final LimitedReservationRepository limitedReservationRepository;
    private final LimitedEventRepository limitedEventRepository;
    private final UserService userService;
    private final LimitedReservationQueueService queueService;
    private final LimitedReservationExpireQueueService expireQueueService;

    private static final int EXPIRATION_HOURS = 24;
    private final RedissonLockService redissonLockService;

    /*
     * 예약 생성
     */
    @Transactional
    public LimitedReservationResponse createReservation(AuthUser authUser,  LimitedReservationCreateRequest request) {
        String lockKey=  "lock:event" + request.limitedEventId();

        // Redisson 분산 락 적용
        return redissonLockService.runWithLock(lockKey,() -> createReservationWithLock(authUser, request));
    }

    public LimitedReservationResponse createReservationWithLock(AuthUser authUser, LimitedReservationCreateRequest request) {
        LimitedEvent limitedEvent = findEvent(request.limitedEventId());
        validateNotAlreadyReserved(authUser.getUserId(), limitedEvent);

        Users user = userService.findByIdOrElseThrow(authUser.getUserId());
        Instant expiresAt = Instant.now().plus(EXPIRATION_HOURS,  ChronoUnit.HOURS);

        long successCount = limitedReservationRepository.countByLimitedEventAndStatus(limitedEvent, LimitedReservationStatus.SUCCESS);
        boolean isReservable = limitedEvent.canAcceptReservation(successCount);

        LimitedReservation reservation = isReservable
                ? createSuccessReservation(user, limitedEvent, expiresAt)
                : createWaitingReservation(user, limitedEvent, expiresAt);

        limitedReservationRepository.save(reservation);
        handlePostReservationActions(reservation);

        return LimitedReservationResponse.of(reservation);
    }

    /*
     * 상태별 예약 생성 메서드
     */
    private LimitedReservation createSuccessReservation(Users user, LimitedEvent limitedEvent, Instant expiresAt) {
        LimitedReservation reservation = LimitedReservation.of(user, limitedEvent, LimitedReservationStatus.SUCCESS, expiresAt);
        limitedEvent.decreaseBookQuantity(1);

        return reservation;
    }

    private LimitedReservation createWaitingReservation(Users user, LimitedEvent limitedEvent, Instant expiresAt) {
        return LimitedReservation.of(user, limitedEvent, LimitedReservationStatus.WAITING, expiresAt);
    }

    /*
     * 예약 후 큐 등록 처리
     */
    private void handlePostReservationActions(LimitedReservation reservation) {
        if (reservation.getStatus() == LimitedReservationStatus.SUCCESS) {
            registerExpireQueue(reservation);
        } else if (reservation.getStatus() == LimitedReservationStatus.WAITING) {
            enqueueWaitingReservation(reservation);
        }
    }

    /*
     * Redis 만료  큐 등록
     */
    private void registerExpireQueue(LimitedReservation reservation) {
        expireQueueService.addToExpireQueue(reservation.getLimitedEvent().getId(), reservation.getId(), reservation.getExpiresAt());
    }

    /*
     * Redis 대기열 큐 등록
     */
    private void enqueueWaitingReservation(LimitedReservation reservation) {
        queueService.enqueue(reservation.getLimitedEvent().getId(), reservation.getId(), Instant.now());
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
    @Transactional(readOnly = true)
    public List<LimitedReservationResponse> getReservationsByEventAndStatus(Long eventId, List<LimitedReservationStatus> statuses) {
        LimitedEvent limitedEvent = findEvent(eventId);
        List<LimitedReservation> reservations = limitedReservationRepository.findAllByLimitedEventAndStatusIn(limitedEvent, statuses);

        return reservations.stream()
                .map(LimitedReservationResponse::of)
                .toList();
    }

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

        // Redis 대기열에서  해당 예약제거
        queueService.remove(reservation.getLimitedEvent().getId(), reservation.getId());
        expireQueueService.removeFromExpireQueue(reservation.getLimitedEvent().getId(), reservation.getId());
        promoteNextWaitingReservation(reservation.getLimitedEvent().getId());
    }

    @Transactional
    public void expireReservationAndPromote() {
        Instant now = Instant.now();
        List<LimitedReservation> expired = limitedReservationRepository.findAllByStatusAndExpiredAtBefore(LimitedReservationStatus.SUCCESS, now);

        for (LimitedReservation reservation : expired) {
            reservation.markCanceled();

            queueService.remove(reservation.getLimitedEvent().getId(),reservation.getId());

            promoteNextWaitingReservation(reservation.getLimitedEvent().getId());
        }
    }

    @Transactional
    public void expireReservationAndPromoteOne(Long reservationId) {
      LimitedReservation reservation = findReservation(reservationId);

      if (reservation.getStatus() != LimitedReservationStatus.SUCCESS || !reservation.isExpired())
          return;

      reservation.markCanceled();
      queueService.remove(reservation.getLimitedEvent().getId(), reservation.getId());
      promoteNextWaitingReservation(reservation.getLimitedEvent().getId());
    }

    // --- 내부 메서드 ---
    private void validateNotAlreadyReserved(Long userId, LimitedEvent event) {
        limitedReservationRepository.findByUsersIdAndLimitedEvent(userId, event)
                .ifPresent(r -> {
                    throw new BadRequestException(ALREADY_RESERVED_EVENT.getMessage());
                });
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

    private void promoteNextWaitingReservation(Long limitedEventId) {
        Long nextReservationId  = queueService.dequeue(limitedEventId);
        if (nextReservationId == null) return;

        LimitedReservation waiting =  limitedReservationRepository.findById(nextReservationId).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_RESERVATION.getMessage())
        );

        waiting.markSuccess();
        waiting.getLimitedEvent().decreaseBookQuantity(1);
    }
}
