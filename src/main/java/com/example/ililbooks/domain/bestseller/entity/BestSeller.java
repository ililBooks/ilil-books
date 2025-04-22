package com.example.ililbooks.domain.bestseller.entity;

import com.example.ililbooks.domain.bestseller.enums.PeriodType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "bestsellers")
public class BestSeller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookId;

    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    private Instant date;

    private int ranking;

    @Builder
    public BestSeller(Long bookId, PeriodType periodType, Instant date, int ranking) {
        this.bookId = bookId;
        this.periodType = periodType;
        this.date = date;
        this.ranking = ranking;
    }
}
