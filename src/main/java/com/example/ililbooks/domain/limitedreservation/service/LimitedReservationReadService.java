package com.example.ililbooks.domain.limitedreservation.service;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedevent.repository.LimitedEventRepository;
import com.example.ililbooks.domain.limitedreservation.dto.request.LimitedReservationStatusFilterRequest;
import com.example.ililbooks.domain.limitedreservation.dto.response.*;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import com.example.ililbooks.domain.limitedreservation.repository.LimitedReservationRepository;
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
public class LimitedReservationReadService {

    private final LimitedReservationRepository reservationRepository;
    private final LimitedEventRepository eventRepository;

    /*/ 내 예약 상세 조회 */
    @Transactional(readOnly = true)
    public LimitedReservationResponse getReservationByUser(AuthUser authUser, Long reservationId) {
        LimitedReservation reservation = findOwnReservation(reservationId, authUser.getUserId());
        return LimitedReservationResponse.of(reservation);
    }

    /*/ 내 예약 상태 단건 조회 */
    @Transactional(readOnly = true)
    public LimitedReservationStatusResponse getReservationStatus(AuthUser authUser, Long reservationId) {
        LimitedReservation reservation = findOwnReservation(reservationId, authUser.getUserId());
        return LimitedReservationStatusResponse.of(reservation);
    }

    /*/ 행사별 예약 전체 조회 */
    @Transactional(readOnly = true)
    public Page<LimitedReservationResponse> getReservationsByEvent(Long eventId, Pageable pageable) {
        LimitedEvent event = findEventByIdOrElseThrow(eventId);
        return reservationRepository.findAllByLimitedEvent(event, pageable)
                .map(LimitedReservationResponse::of);
    }

    /*/ 행사별 + 상태별 예약 리스트 조회 */
    @Transactional(readOnly = true)
    public List<LimitedReservationResponse> getReservationsByEventAndStatus(Long eventId, List<LimitedReservationStatus> statuses) {
        LimitedEvent event = findEventByIdOrElseThrow(eventId);
        return reservationRepository.findAllByLimitedEventAndStatusIn(event, statuses)
                .stream()
                .map(LimitedReservationResponse::of)
                .toList();
    }

    /*/ 예약 통계 조회 (출판사/관리자) */
    @Transactional(readOnly = true)
    public LimitedReservationSummaryResponse getReservationSummary(Long eventId) {
        LimitedEvent event = findEventByIdOrElseThrow(eventId);

        long success = reservationRepository.countByLimitedEventAndStatus(event, LimitedReservationStatus.RESERVED);
        long waiting = reservationRepository.countByLimitedEventAndStatus(event, LimitedReservationStatus.WAITING);
        long canceled = reservationRepository.countByLimitedEventAndStatus(event, LimitedReservationStatus.CANCELED);

        return LimitedReservationSummaryResponse.of(event, success, waiting, canceled);
    }

    /*/ 필터 기반 예약 목록 조회 */
    @Transactional(readOnly = true)
    public List<LimitedReservationResponse> getReservationsByFilter(LimitedReservationStatusFilterRequest request) {
        if (request.eventId() == null || request.statuses() == null || request.statuses().isEmpty()) {
            throw new BadRequestException("이벤트 ID 및 상태 목록은 필수입니다.");
        }

        return reservationRepository.findByFilter(request.eventId(), request.statuses(), request.userId(), request.startDate(), request.endDate())
                .stream()
                .map(LimitedReservationResponse::of)
                .toList();
    }

    // ---- 내부 메서드 ----

    private LimitedReservation findOwnReservation(Long id, Long userId) {
        LimitedReservation reservation = findReservationByIdOrElseThrow(id);
        if (!reservation.getUsers().getId().equals(userId)) {
            throw new BadRequestException(NOT_OWN_RESERVATION.getMessage());
        }
        return reservation;
    }

    public LimitedReservation findReservationByIdOrElseThrow(Long id) {
        return reservationRepository.findById(id).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_RESERVATION.getMessage()));
    }

    private LimitedEvent findEventByIdOrElseThrow(Long id) {
        return eventRepository.findById(id).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_EVENT.getMessage()));
    }
}
