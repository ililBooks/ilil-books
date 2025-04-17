package com.example.ililbooks.domain.limitedreservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LimitedReservationQueueService {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "queue:reservation:event:";

    /*
     * 대기열에 예약 추가
     */
    public void enqueue(Long eventId, Long reservationId, Instant timestamp) {
        String key = getKey(eventId);
        redisTemplate.opsForZSet().add(key, reservationId.toString(), timestamp.toEpochMilli());
    }

    /*
     * 가장 오래된 예약 하나 꺼내기
     */
    public Long dequeue(Long  limitedEventId) {
        String key = getKey(limitedEventId);
        ZSetOperations<String, String> zSet = redisTemplate.opsForZSet();

        Set<String> ids = zSet.range(key, 0, 0); // 가장 먼저 들어온 1개
        if (ids == null || ids.isEmpty()) return null;

        String reservationId = ids.iterator().next();
        zSet.remove(key, reservationId);
        return Long.parseLong(reservationId);
    }

    /*
     * 대기열에서 예약 제거
     */
    public void remove(Long eventId, Long reservationId) {
        String key = getKey(eventId);
        redisTemplate.opsForZSet().remove(key, reservationId.toString());
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
