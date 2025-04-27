package com.example.ililbooks.domain.order.entity;

import com.example.ililbooks.domain.order.enums.DeliveryStatus;
import com.example.ililbooks.domain.order.enums.LimitedType;
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

    private String name;

    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", columnDefinition = "VARCHAR(50)")
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", columnDefinition = "VARCHAR(50)")
    private DeliveryStatus deliveryStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", columnDefinition = "VARCHAR(50)")
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "limited_type", columnDefinition = "VARCHAR(50)")
    private LimitedType limitedType;

    @Builder
    public Order(Long id, Users users, String number, String name, BigDecimal totalPrice, OrderStatus orderStatus, DeliveryStatus deliveryStatus, PaymentStatus paymentStatus, LimitedType limitedType) {
        this.id = id;
        this.users = users;
        this.number = number;
        this.name = name;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.deliveryStatus = deliveryStatus;
        this.paymentStatus = paymentStatus;
        this.limitedType = limitedType;
    }

    public static Order of(Users users, String name, BigDecimal totalPrice, LimitedType limitedType) {
        return Order.builder()
                .users(users)
                .number(UUID.randomUUID().toString())
                .name(name)
                .totalPrice(totalPrice)
                .orderStatus(OrderStatus.PENDING)
                .deliveryStatus(DeliveryStatus.READY)
                .paymentStatus(PaymentStatus.PENDING)
                .limitedType(limitedType)
                .build();
    }

    public void updateOrder(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void updatePayment(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void updateDelivery(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
}