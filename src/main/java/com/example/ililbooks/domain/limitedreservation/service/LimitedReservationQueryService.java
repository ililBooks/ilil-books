package com.example.ililbooks.domain.limitedreservation.service;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedevent.repository.LimitedEventRepository;
import com.example.ililbooks.domain.limitedreservation.dto.request.LimitedReservationStatusFilterRequest;
import com.example.ililbooks.domain.limitedreservation.dto.response.*;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import com.example.ililbooks.domain.limitedreservation.repository.LimitedReservationRepository;
import com.example.ililbooks.domain.limitedreservation.repository.LimitedReservationStatusHistoryRepository;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class LimitedReservationQueryService {

    private final LimitedReservationRepository reservationRepository;
    private final LimitedEventRepository eventRepository;
    private final LimitedReservationStatusHistoryRepository historyRepository;

    /*/ 예약 단건 조회 */
    @Transactional(readOnly = true)
    public LimitedReservationResponse getReservationByUser(AuthUser authUser, Long reservationId) {
        LimitedReservation reservation = findReservation(reservationId);

        if (!reservation.getUsers().getId().equals(authUser.getUserId())) {
            throw new BadRequestException(NOT_OWN_RESERVATION.getMessage());
        }
        return LimitedReservationResponse.of(reservation);
    }

    /*/ 행사별 예약 전체 조회 (페이징) */
    @Transactional(readOnly = true)
    public Page<LimitedReservationResponse> getReservationsByEvent(Long eventId, Pageable pageable) {
        LimitedEvent event = findEvent(eventId);
        return reservationRepository.findAllByLimitedEvent(event, pageable)
                .map(LimitedReservationResponse::of);
    }

    /*/ 행사별 + 상태별 예약 리스트 조회 */
    @Transactional(readOnly = true)
    public List<LimitedReservationResponse> getReservationsByEventAndStatus(Long eventId, List<LimitedReservationStatus> statuses) {
        LimitedEvent event = findEvent(eventId);
        return reservationRepository.findAllByLimitedEventAndStatusIn(event, statuses)
                .stream()
                .map(LimitedReservationResponse::of).toList();
    }

    /*/ 예약 상태 변경 이력 조회 */
    @Transactional(readOnly = true)
    public List<LimitedReservationStatusHistoryResponse> getReservationStatusHistory(Long reservationId) {
        findReservation(reservationId);
        return historyRepository.findAllByReservationIdOrderByCreatedAtDesc(reservationId)
                .stream()
                .map(LimitedReservationStatusHistoryResponse::of).toList();
    }

    /*/ 필터 기반 예약 목록 조회 */
    @Transactional(readOnly = true)
    public List<LimitedReservationResponse> getReservationsByFilter(LimitedReservationStatusFilterRequest request) {
        return reservationRepository.findByFilter(request.eventId(), request.statuses(), request.userId(), request.startDate(), request.endDate())
                .stream()
                .map(LimitedReservationResponse::of).toList();
    }

    /*/ 내 예약 단건 조회 */
    @Transactional(readOnly = true)
    public LimitedReservationStatusResponse getReservationStatus(AuthUser authUser, Long reservationId) {
        LimitedReservation reservation = findReservation(reservationId);

        if (!reservation.getUsers().getId().equals(authUser.getUserId())) {
            throw new BadRequestException(NOT_OWN_RESERVATION.getMessage());
        }

        return LimitedReservationStatusResponse.of(reservation);
    }

    /*/ 행사별 예약 상태 통계 조회 */
    @Transactional(readOnly = true)
    public LimitedReservationSummaryResponse getReservationSummary(Long eventId) {
        LimitedEvent event = findEvent(eventId);

        long success = reservationRepository.countByLimitedEventAndStatus(event, LimitedReservationStatus.SUCCESS);
        long waiting = reservationRepository.countByLimitedEventAndStatus(event, LimitedReservationStatus.WAITING);
        long canceled = reservationRepository.countByLimitedEventAndStatus(event, LimitedReservationStatus.CANCELED);

        return LimitedReservationSummaryResponse.of(event, success, waiting, canceled);
    }

    // ---- 내부 메서드 ----

    private LimitedEvent findEvent(Long id) {
        return eventRepository.findById(id).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_EVENT.getMessage()));
    }

    private LimitedReservation findReservation(Long id) {
        return reservationRepository.findById(id).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_RESERVATION.getMessage()));
    }
}
