package com.example.ililbooks.domain.limitedreservation.processor;

import com.example.ililbooks.domain.limitedreservation.service.LimitedReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpirationReservationProcessor {

    private final LimitedReservationService limitedReservationService;

    /*
     * 10 초마다 예약 만료 상태 확인 및 처리
     */
    @Scheduled(fixedDelay = 10000)
    public void checkExpiredReservations() {
        log.info("[예약 만료 배치] 실행 시작");
        limitedReservationService.expireReservationAndPromote();
        log.info("[예약 만료 배치] 실행 완료");
    }
}
