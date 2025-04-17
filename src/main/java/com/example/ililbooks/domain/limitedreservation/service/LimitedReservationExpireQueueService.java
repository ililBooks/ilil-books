package com.example.ililbooks.domain.limitedreservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LimitedReservationExpireQueueService {

    private static final String KEY_PREFIX = "expire:reservation:event:";

    private final StringRedisTemplate redisTemplate;

    /*
     * 예약 만료 큐에 추가 (예약 생성시)
     */
    public void addToExpireQueue(Long limitedEventId, Long reservationId, Instant expiredAt) {
        String key = KEY_PREFIX + limitedEventId;
        redisTemplate.opsForZSet().add(key, reservationId.toString(), expiredAt.toEpochMilli());
    }

    /*
     * 예약 만료 큐에서 제거 (예약 취소 or 만료 후)
     */
    public void removeFromExpireQueue(Long limitedEventId, Long reservationId) {
        String key = KEY_PREFIX + limitedEventId;
        redisTemplate.opsForZSet().remove(key, reservationId.toString());
    }
}
