package com.example.ililbooks.domain.limitedreservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class LimitedReservationExpireQueueService {

    private static final String KEY_PREFIX = "expire:reservation:event:";

    private final StringRedisTemplate redisTemplate;

    /*
     * 예약 만료 큐에 추가 (예약 생성시)
     */
    public void addToExpireQueue(Long limitedEventId, Long reservationId, Instant expiredAt) {
        if (limitedEventId == null || reservationId == null || expiredAt == null) return;

        String key = KEY_PREFIX + limitedEventId;
        redisTemplate.opsForZSet().add(key, reservationId.toString(), expiredAt.toEpochMilli());

        log.info("[EXPIRE-QUEUE ADD] eventId={}, reservationId={}, score={}", limitedEventId, reservationId, expiredAt.toEpochMilli());
    }

    /*
     * 예약 만료 큐에서 제거 (예약 취소 or 만료 후)
     */
    public void removeFromExpireQueue(Long limitedEventId, Long reservationId) {
        if (limitedEventId == null || reservationId == null) return;

        String key = KEY_PREFIX + limitedEventId;
        redisTemplate.opsForZSet().remove(key, reservationId.toString());

        log.info("[EXPIRE-QUEUE REMOVE] eventId={}, reservationId={}", limitedEventId, reservationId);
    }
}
