package com.example.ililbooks.domain.limitedevent.repository;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LimitedEventRepository extends JpaRepository<LimitedEvent, Long> {

    Optional<LimitedEvent> findByIdAndIsDeletedFalse(Long id);

    Page<LimitedEvent> findAllByIsDeletedFalse(Pageable pageable);

    @Query("SELECT e FROM LimitedEvent e " +
            "JOIN FETCH e.book b " +
            "JOIN FETCH b.users u " +
            "WHERE e.id = :id AND e.isDeleted = false")
    Optional<LimitedEvent> findByIdWithBookAndUser(@Param("id") Long id);
}
