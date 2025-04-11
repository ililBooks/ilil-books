package com.example.ililbooks.domain.order.repository;

import com.example.ililbooks.domain.order.entity.OrderHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
    Page<OrderHistory> findAllByOrderId(Long orderId, Pageable pageable);
    List<OrderHistory> findAllByOrderId(Long orderId);
}
