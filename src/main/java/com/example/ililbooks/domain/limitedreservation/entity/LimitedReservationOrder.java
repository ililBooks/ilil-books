package com.example.ililbooks.domain.limitedreservation.entity;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.order.enums.DeliveryStatus;
import com.example.ililbooks.domain.order.enums.OrderStatus;
import com.example.ililbooks.domain.order.enums.PaymentStatus;
import com.example.ililbooks.global.entity.TimeStamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LimitedReservationOrder extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private LimitedReservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    public static LimitedReservationOrder of(LimitedReservation reservation, Book book, BigDecimal price, int quantity) {
        return new LimitedReservationOrder(
                reservation,
                book,
                price.multiply(BigDecimal.valueOf(quantity)),
                OrderStatus.ORDERED,
                DeliveryStatus.READY,
                PaymentStatus.PAID
        );
    }

    @Builder
    private LimitedReservationOrder(LimitedReservation reservation, Book book, BigDecimal totalPrice,
                                    OrderStatus orderStatus, DeliveryStatus deliveryStatus, PaymentStatus paymentStatus) {
        this.reservation = reservation;
        this.book = book;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.deliveryStatus = deliveryStatus;
        this.paymentStatus = paymentStatus;
    }
}