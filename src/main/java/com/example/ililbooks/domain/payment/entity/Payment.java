package com.example.ililbooks.domain.payment.entity;

import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.payment.enums.PGProvider;
import com.example.ililbooks.domain.payment.enums.PayStatus;
import com.example.ililbooks.domain.payment.enums.PaymentMethod;
import com.example.ililbooks.global.entity.TimeStamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payments")
public class Payment extends TimeStamped {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private String impUid;
    private String merchantUid;

    @Enumerated(EnumType.STRING)
    private PGProvider pg;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String buyerEmail;
    private String buyerName;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PayStatus payStatus;

    private LocalDateTime paidAt;

    @Builder
    private Payment(Long id, Order order, String impUid, String merchantUid, PGProvider pg, PaymentMethod paymentMethod, String buyerEmail, String buyerName, BigDecimal amount, PayStatus payStatus, LocalDateTime paidAt) {
        this.id = id;
        this.order = order;
        this.impUid = impUid;
        this.merchantUid = merchantUid;
        this.pg = pg;
        this.paymentMethod = paymentMethod;
        this.buyerEmail = buyerEmail;
        this.buyerName = buyerName;
        this.amount = amount;
        this.payStatus = payStatus;
        this.paidAt = paidAt;
    }
}

