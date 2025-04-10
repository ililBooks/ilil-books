package com.example.ililbooks.domain.cart.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash(value = "cart", timeToLive = 60 * 60 * 24)
public class Cart {

    @Id
    private Long userId;
    private Map<Long, CartItem> items = new HashMap<>();

    public Cart(Long userId) {
        this.userId = userId;
    }

    @Builder
    public Cart(Long userId, Map<Long, CartItem> items) {
        this.userId = userId;
        this.items = items;
    }
}
