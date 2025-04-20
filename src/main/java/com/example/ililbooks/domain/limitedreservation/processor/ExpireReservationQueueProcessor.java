package com.example.ililbooks.domain.limitedreservation.processor;

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
    @Scheduled(fixedDelay = POLL_INTERVAL_MILLIS)
    public void pollExpireQueue() {
        log.debug("[ 만료 큐 polling] 시작");

        for (long limitedEventId = 1; limitedEventId <= 10000; limitedEventId++) { // 1~10000 ID 기준 Loop
            String key = EXPIRE_KEY_PREFIX + limitedEventId;

            Set<String> expired = redisTemplate.opsForZSet().rangeByScore(key, 0, Instant.now().toEpochMilli());

            if (expired == null || expired.isEmpty()) continue;

            for (String reservationIdStr : expired) {
                redisTemplate.opsForZSet().remove(key, reservationIdStr); //Zset 에서 제거

                try {
                    Long reservationId = Long.parseLong(reservationIdStr);
                    limitedReservationService.expireReservationAndPromoteOne(reservationId);
                } catch (NumberFormatException e) {
                    log.warn("잘못된 예약 ID: {}", reservationIdStr);
                }
            }
        }

        log.debug("[만료 큐 polling] 완료");
    }
}
