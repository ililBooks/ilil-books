package com.example.ililbooks.domain.order.entity;

import com.example.ililbooks.domain.order.enums.DeliveryStatus;
import com.example.ililbooks.domain.order.enums.OrderStatus;
import com.example.ililbooks.domain.order.enums.PaymentStatus;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.global.entity.TimeStamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users users;

    private String number;

    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Builder
    private Order(Long id, Users users, String number, BigDecimal totalPrice, OrderStatus orderStatus, DeliveryStatus deliveryStatus, PaymentStatus paymentStatus) {
        this.id = id;
        this.users = users;
        this.number = number;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.deliveryStatus = deliveryStatus;
        this.paymentStatus = paymentStatus;
    }

    public static Order of(Users users, BigDecimal totalPrice) {
        return Order.builder()
                .users(users)
                .number(UUID.randomUUID().toString())
                .totalPrice(totalPrice)
                .orderStatus(OrderStatus.PENDING)
                .deliveryStatus(DeliveryStatus.READY)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
    }

    public void updateOrder(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void updateDelivery(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
}