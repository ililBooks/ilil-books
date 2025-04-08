package com.example.ililbooks.domain.cart.service;

import com.example.ililbooks.domain.cart.entity.Cart;
import com.example.ililbooks.global.exception.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.Duration;

import static com.example.ililbooks.global.exception.ErrorMessage.INTERNAL_SERVER_ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisClient {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration CART_TTL = Duration.ofMinutes(30);
    private static final String CART_KEY_PREFIX = "cart:";

    public <T> T get(Long key, Class<T> classType) {
        return get(key.toString(), classType);
    }

    private <T> T get(String key, Class<T> classType) {
        String redisKey = CART_KEY_PREFIX + key;
        String redisValue = (String) redisTemplate.opsForValue().get(redisKey);

        if (ObjectUtils.isEmpty(redisValue)) {
            return null;
        }

        try {
            return objectMapper.readValue(redisValue, classType);
        } catch (JsonProcessingException e) {
            log.error("Redis value parsing error: {}", redisValue, e);
            throw new BadRequestException(INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    public void put(Long key, Object value) {
        put(key.toString(), value);
    }

    private void put(String key, Object value) {
        String redisKey = CART_KEY_PREFIX + key;
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(redisKey, json, CART_TTL);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize value: {}", value, e);
            throw new BadRequestException(INTERNAL_SERVER_ERROR.getMessage());
        }
    }
}

