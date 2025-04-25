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
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

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

    @Column(unique = true)
    private String impUid;

    @Column(unique = true)
    private String merchantUid;

    @Enumerated(EnumType.STRING)
    @Column(name = "pg", columnDefinition = "VARCHAR(50)")
    private PGProvider pg;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", columnDefinition = "VARCHAR(50)")
    private PaymentMethod paymentMethod;

    private String buyerEmail;
    private String buyerName;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_status", columnDefinition = "VARCHAR(50)")
    private PayStatus payStatus;

    private Instant paidAt;

    @Builder
    private Payment(Long id, Order order, String impUid, String merchantUid, PGProvider pg, PaymentMethod paymentMethod, String buyerEmail, String buyerName, BigDecimal amount, PayStatus payStatus, Instant paidAt) {
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

    public static Payment of(Order order, String impUid, PGProvider pg, PaymentMethod paymentMethod) {
        return Payment.builder()
                .order(order)
                .impUid(impUid)
                .merchantUid("merchantUid_" + UUID.randomUUID().toString().substring(0,8))
                .pg(pg)
                .paymentMethod(paymentMethod)
                .buyerEmail(order.getUsers().getEmail())
                .buyerName(order.getUsers().getNickname())
                .amount(order.getTotalPrice())
                .payStatus(PayStatus.READY)
                .paidAt(null)
                .build();
    }

    public void updateSuccessPayment(String impUid) {
        this.impUid = impUid;
        this.payStatus = PayStatus.PAID;
        this.paidAt = Instant.now();
    }

    public void updateFailPayment(String impUid) {
        this.impUid = impUid;
        this.payStatus = PayStatus.FAILED;
    }
}

