package com.example.ililbooks.domain.order.repository;

import com.example.ililbooks.domain.order.entity.OrderHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
    Page<OrderHistory> findAllByOrderId(Long orderId, Pageable pageable);

    @Query("SELECT oh FROM OrderHistory oh JOIN FETCH oh.book WHERE oh.order.id = :orderId")
    List<OrderHistory> findAllByOrderId(@Param("orderId") Long orderId);
}
