package com.example.ililbooks.domain.limitedevent.scheduler;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedevent.enums.LimitedEventStatus;
import com.example.ililbooks.domain.limitedevent.repository.LimitedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LimitedEventStatusUpdater {

    private final LimitedEventRepository limitedEventRepository;

    /*
     * 매 1분마다 모든 이벤트 상태 점검
     */
    @Transactional
    @Scheduled(fixedDelay = 60_000)
    public void updateEventStatuses() {
        List<LimitedEvent> limitedEvents = limitedEventRepository.findAllByIsDeletedFalse(Pageable.unpaged()).getContent();

        Instant now = Instant.now();

        for (LimitedEvent limitedEvent : limitedEvents) {
            if (limitedEvent.getStatus() == LimitedEventStatus.ENDED) continue;

            if (now.isAfter(limitedEvent.getEndTime())) {
                limitedEvent.updateStatus(LimitedEventStatus.ENDED);
                log.info("[이벤트 상태 변경] {} → ENDED (id={})", limitedEvent.getStatus(), limitedEvent.getId());
            } else if (now.isAfter(limitedEvent.getStartTime()) && limitedEvent.getStatus() == LimitedEventStatus.INACTIVE) {
                limitedEvent.updateStatus(LimitedEventStatus.ACTIVE);
                log.info("[이벤트 상태 변경] INACTIVE → ACTIVE (id={})", limitedEvent.getId());
            }
        }
    }
}
