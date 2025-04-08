package com.example.ililbooks.domain.limitedevent.repository;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LimitedEventRepository extends JpaRepository<LimitedEvent, Long> {

    Optional<LimitedEvent> findByIdAndDeletedAtIsNull(Long limitedEventId);

    Page<LimitedEvent> findAllByDeletedAtIsNull(Pageable pageable);
}
