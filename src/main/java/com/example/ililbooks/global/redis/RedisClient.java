package com.example.ililbooks.global.redis;

import com.example.ililbooks.global.exception.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.time.Duration;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Component
@RequiredArgsConstructor
public class RedisClient {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public <T> T get(String key, Class<T> classType) {
        String redisValue = (String) redisTemplate.opsForValue().get(key);

        if (ObjectUtils.isEmpty(redisValue)) {
            return null;
        }
        try {
            return objectMapper.readValue(redisValue, classType);
        } catch (JsonProcessingException e) {
            throw new BadRequestException(REDIS_PARSING_FAILED.getMessage());
        }
    }

    public void put(String key, Object value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (JsonProcessingException e) {
            throw new BadRequestException(REDIS_SERIALIZE_FAILED.getMessage());
        }
    }
}

