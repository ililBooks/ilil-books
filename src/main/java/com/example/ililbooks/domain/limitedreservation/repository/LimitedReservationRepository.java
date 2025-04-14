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

import java.util.Optional;

public interface LimitedReservationRepository extends JpaRepository<LimitedReservation, Long> {

    Optional<LimitedReservation> findByUsersAndLimitedEvent(Users user, LimitedEvent limitedEvent);

    Optional<LimitedReservation> findByUsersIdAndLimitedEvent(Long userId, LimitedEvent limitedEvent);

    Long countByLimitedEventAndStatus(LimitedEvent limitedEvent, LimitedReservationStatus status);

    Page<LimitedReservation> findAllByLimitedEvent(LimitedEvent limitedEvent, Pageable pageable);

    @Query("SELECT r FROM LimitedReservation r JOIN r.limitedEvent e WHERE r.id = :id")
    Optional<LimitedReservation> findByIdWithEvent(@Param("id") Long id);

}