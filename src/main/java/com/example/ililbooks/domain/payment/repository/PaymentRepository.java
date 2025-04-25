package com.example.ililbooks.domain.payment.repository;

import com.example.ililbooks.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByMerchantUid(String merchantUid);

    @Query("SELECT p FROM Payment p JOIN FETCH Order o " +
            "WHERE p.id = :paymentId")
    Optional<Payment> findById(@Param("paymentId")String paymentId);
}
