package com.example.ililbooks.domain.limitedreservation.processor;

import com.example.ililbooks.domain.limitedreservation.service.LimitedReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpireReservationQueueProcessor {

    private static final String EXPIRE_KEY_PREFIX = "expire:reservation:event:";
    private static final long POLL_INTERVAL_MILLIS = 10_000L;

    private final StringRedisTemplate  redisTemplate;
    private final LimitedReservationService limitedReservationService;

    /*
     * Redis 큐에서 만료 예약을 polling
     * 예약 자동 취소 + 대기자 승급
     */
    @Scheduled(fixedDelay = POLL_INTERVAL_MILLIS, initialDelay = 10_000)
    public void pollExpireQueue() {
        log.debug("[만료 큐 polling] 시작");

        Set<String> keys = redisTemplate.keys(EXPIRE_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            Long eventId = extractEventId(key);
            if (eventId == null) continue;

            Set<String> expired = redisTemplate.opsForZSet().rangeByScore(key, 0, Instant.now().toEpochMilli());

            if (expired == null || expired.isEmpty()) continue;

            for (String reservationIdStr : expired) {
                redisTemplate.opsForZSet().remove(key, reservationIdStr); // ZSet에서 제거

                try {
                    Long reservationId = Long.parseLong(reservationIdStr);
                    limitedReservationService.expireReservationAndPromoteOne(reservationId);
                } catch (Exception e) {
                    log.warn("[만료 큐] 처리 실패 - reservationId={}, error={}", reservationIdStr, e.getMessage(), e);
                }
            }

            log.info("[만료 큐 처리 완료] eventId={}, 처리된 예약 수={}", eventId, expired.size());
        }

        log.debug("[만료 큐 polling] 완료");
    }

    private Long extractEventId(String key) {
        try {
            return Long.parseLong(key.substring(EXPIRE_KEY_PREFIX.length()));
        } catch (Exception e) {
            log.warn("잘못된 Redis 키 형식: {}", key);
            return null;
        }
    }
}