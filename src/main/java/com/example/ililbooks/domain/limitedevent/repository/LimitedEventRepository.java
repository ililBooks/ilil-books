package com.example.ililbooks.domain.limitedevent.repository;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LimitedEventRepository extends JpaRepository<LimitedEvent, Long> {
}
