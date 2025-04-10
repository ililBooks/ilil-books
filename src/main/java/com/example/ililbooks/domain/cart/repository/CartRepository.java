package com.example.ililbooks.domain.cart.repository;

import com.example.ililbooks.domain.cart.entity.Cart;
import com.example.ililbooks.global.redis.RedisClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class CartRepository {

    private final RedisClient redisClient;

    private static final Duration CART_TTL = Duration.ofMinutes(60 * 24);        // 하루
    private static final String CART_KEY_PREFIX = "cart:";

    /* 장바구니 조회 */
    public Cart get(Long userId) {
        String redisKey = CART_KEY_PREFIX + userId;
        return redisClient.get(redisKey, Cart.class);
    }

    /* 장바구니 저장 및 수정 */
    public void put(Long userId, Cart cart) {
        String redisKey = CART_KEY_PREFIX + userId;
        redisClient.put(redisKey, cart, CART_TTL);
    }

    /* 장바구니 비우기 */
    public void clear(Long userId) {
        String redisKey = CART_KEY_PREFIX + userId;
        redisClient.delete(redisKey);
    }
}
