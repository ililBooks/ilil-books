package com.example.ililbooks.domain.limitedreservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LimitedReservationQueueService {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "queue:reservation:event:";

    /*
     * 대기열에 예약 추가
     */
    public void enqueue(Long eventId, Long reservationId, Instant timestamp) {
        if (eventId == null || reservationId == null || timestamp == null) return;

        String key = getKey(eventId);
        boolean result = Boolean.TRUE.equals(
                redisTemplate.opsForZSet().add(key, reservationId.toString(), timestamp.toEpochMilli())
        );

        if (result) {
            log.info("[ENQUEUE] eventId={}, reservationId={}, score={}", eventId, reservationId, timestamp.toEpochMilli());
        }
    }

    /*
     * 가장 오래된 예약 하나 꺼내기
     */
    public Long dequeue(Long limitedEventId) {
        String key = getKey(limitedEventId);
        Set<String> ids = redisTemplate.opsForZSet().range(key, 0, 0); // 가장 먼저 들어온 1개

        if (ids == null || ids.isEmpty()) return null;

        String reservationId = ids.iterator().next();
        redisTemplate.opsForZSet().remove(key, reservationId);

        log.info("[DEQUEUE] eventId={}, dequeuedId={}", limitedEventId, reservationId);
        return Long.parseLong(reservationId);
    }

    /*
     * 대기열에서 예약 제거
     */
    public void remove(Long eventId, Long reservationId) {
        if (eventId == null || reservationId == null) return;

        String key = getKey(eventId);
        redisTemplate.opsForZSet().remove(key, reservationId.toString());

        log.info("[REMOVE] eventId={}, reservationId={}", eventId, reservationId);
    }

    /*
     * Redis 키 설정
     */
    private String getKey(Long limitedEventId) {
        return KEY_PREFIX + limitedEventId;
    }

    /*
     * 디버깅용 대기열 전체 보기
     */
    public Set<String> getWaitingList(Long limitedEventId) {
        return redisTemplate.opsForZSet().range(getKey(limitedEventId), 0, -1);
    }
}
