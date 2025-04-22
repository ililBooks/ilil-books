package com.example.ililbooks.domain.limitedreservation.processor;

import com.example.ililbooks.domain.limitedreservation.service.LimitedReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpirationReservationProcessor {

    private final LimitedReservationService limitedReservationService;

    /*
     * 10 초마다 예약 만료 상태 확인 및 처리
     */
    @Scheduled(fixedDelay = 10_000, initialDelay = 10_000)
    public void checkExpiredReservations() {
        log.info("[예약 만료 배치] 시작: {}", Instant.now());

        try {
            limitedReservationService.expireReservationAndPromote();
        } catch (Exception e) {
            log.error("[예약 만료 배치] 예외 발생: {}", e.getMessage(), e);
        }

        log.info("[예약 만료 배치] 완료: {}", Instant.now());
    }
}
