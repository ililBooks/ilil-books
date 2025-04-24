package com.example.ililbooks.domain.limitedreservation.repository;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import com.example.ililbooks.domain.user.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LimitedReservationRepository extends JpaRepository<LimitedReservation, Long> {

    Optional<LimitedReservation> findByUsersAndLimitedEvent(Users user, LimitedEvent limitedEvent);

    Optional<LimitedReservation> findByUsersIdAndLimitedEvent(Long userId, LimitedEvent limitedEvent);

    Long countByLimitedEventAndStatus(LimitedEvent limitedEvent, LimitedReservationStatus status);

    Page<LimitedReservation> findAllByLimitedEvent(LimitedEvent limitedEvent, Pageable pageable);

    List<LimitedReservation> findAllByLimitedEventAndStatusIn(LimitedEvent limitedEvent, List<LimitedReservationStatus> statuses);

    @Query("SELECT r FROM LimitedReservation r JOIN r.limitedEvent e WHERE r.id = :id")
    Optional<LimitedReservation> findByIdWithEvent(@Param("id") Long id);

    List<LimitedReservation> findAllByStatusAndExpiresAtBefore(LimitedReservationStatus status, Instant expiredAt);

    /*/ 상태 + 유저 + 생성일 조건 기반 예약 조회 */
    @Query("""
        SELECT r FROM LimitedReservation r
        WHERE r.limitedEvent.id = :eventId
        AND r.status IN :statuses
        AND (:userId IS NULL OR r.users.id = :userId)
        AND (:startDate IS NULL OR r.createdAt >= :startDate)
        AND (:endDate IS NULL OR r.createdAt <= :endDate)
        """) // 텍스트 블럭
    List<LimitedReservation> findByFilter(
            Long eventId,
            List<LimitedReservationStatus> statuses,
            Long userId,
            Instant startDate,
            Instant endDate
    );
}