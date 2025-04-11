package com.example.ililbooks.domain.limitedreservation.repository;

import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservationOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LimitedReservationOrderRepository extends JpaRepository<LimitedReservationOrder, Long> {
}
