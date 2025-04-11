package com.example.ililbooks.domain.limitedreservation.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.limitedreservation.dto.response.LimitedReservationOrderResponse;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservationOrder;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import com.example.ililbooks.domain.limitedreservation.repository.LimitedReservationOrderRepository;
import com.example.ililbooks.domain.limitedreservation.repository.LimitedReservationRepository;
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
     */
    @Transactional
    public LimitedReservationOrderResponse createFromReservation(Long userId, Long reservationId) {
        LimitedReservation reservation = validateReservation(userId, reservationId);

        // 만료된 예약은 주문 불가
        if (reservation.getExpiredAt().isBefore(Instant.now())) {
            throw new BadRequestException(RESERVATION_EXPIRED.getMessage());
        }

        // 이미 주문이 연결되어 있다면 중복 생성 방지
        if (reservation.hasOrder()) {
            throw new BadRequestException(ALREADY_ORDERED.getMessage());
        }

        Book book = getBookFromReservation(reservation);
        validateStock(book);

        LimitedReservationOrder order = LimitedReservationOrder.of(reservation, book, book.getPrice(), 1);
        book.decreaseStock(1);

        limitedReservationOrderRepository.save(order);

        return LimitedReservationOrderResponse.from(order);
    }

    /*
     * 예약 유효성 검증 - 유저 일치, 상태 SUCCESS
     */
    private LimitedReservation validateReservation(Long userId, Long reservationId) {
        LimitedReservation reservation = limitedReservationRepository.findByIdWithEvent(reservationId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_RESERVATION.getMessage()));

        if (!reservation.getUsers().getId().equals(userId)) {
            throw new BadRequestException(NO_PERMISSION.getMessage());
        }

        if (reservation.getStatus() != LimitedReservationStatus.SUCCESS) {
            throw new BadRequestException(RESERVATION_NOT_SUCCESS.getMessage());
        }

        return reservation;
    }

    /*
     * 예약에서 책 가져오기
     */
    private Book getBookFromReservation(LimitedReservation reservation) {
        return reservation.getLimitedEvent().getBook();
    }

    /*
     * 재고 1 이상인지 확인
     */
    private void validateStock(Book book) {
        if (book.getStock() < 1) {
            throw new BadRequestException(OUT_OF_STOCK.getMessage());
        }
    }
}