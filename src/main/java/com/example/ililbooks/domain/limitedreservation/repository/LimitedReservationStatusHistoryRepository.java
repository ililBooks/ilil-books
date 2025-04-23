package com.example.ililbooks.domain.limitedreservation.repository;

import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LimitedReservationStatusHistoryRepository extends JpaRepository<LimitedReservationStatusHistory, Long> {

    List<LimitedReservationStatusHistory> findAllByReservationIdOrderByCreatedAtDesc(Long reservationId);
}
