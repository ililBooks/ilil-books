package com.example.ililbooks.domain.payment.repository;

import com.example.ililbooks.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByMerchantUid(String merchantUid);
}
