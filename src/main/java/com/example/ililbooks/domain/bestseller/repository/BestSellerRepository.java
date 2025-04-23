package com.example.ililbooks.domain.bestseller.repository;

import com.example.ililbooks.domain.bestseller.entity.BestSeller;
import com.example.ililbooks.domain.bestseller.enums.PeriodType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface BestSellerRepository extends JpaRepository<BestSeller, Long> {
    List<BestSeller> findAllByPeriodTypeAndDate(PeriodType periodType, Instant date);
}
