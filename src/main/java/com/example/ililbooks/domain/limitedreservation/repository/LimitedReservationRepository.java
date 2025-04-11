package com.example.ililbooks.domain.limitedreservation.repository;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import com.example.ililbooks.domain.user.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LimitedReservationRepository extends JpaRepository<LimitedReservation, Long> {

    Optional<LimitedReservation> findByUsersAndLimitedEvent(Users user, LimitedEvent limitedEvent);

    Long countByLimitedEventAndStatus(LimitedEvent limitedEvent, LimitedReservationStatus status);

    Page<LimitedReservation> findAllByLimitedEvent(LimitedEvent limitedEvent, Pageable pageable);

    List<LimitedReservation> findAllByLimitedEventAndStatusIn(LimitedEvent limitedEvent, List<LimitedReservationStatus> statuses);

}
