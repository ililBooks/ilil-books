package com.example.ililbooks.domain.limitedevent.service;

import com.example.ililbooks.domain.limitedevent.dto.request.LimitedEventCreateRequest;
import com.example.ililbooks.domain.limitedevent.dto.request.LimitedEventUpdateRequest;
import com.example.ililbooks.domain.limitedevent.dto.response.LimitedEventResponse;
import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedevent.enums.LimitedEventStatus;
import com.example.ililbooks.domain.limitedevent.repository.LimitedEventRepository;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.ililbooks.global.exception.ErrorMessage.NOT_FOUND_TOKEN;

@Service
@RequiredArgsConstructor
public class LimitedEventService {

    private final LimitedEventRepository limitedEventRepository;

    /*
     * 한정판 행사 등록
     */
    @Transactional
    public LimitedEventResponse createLimitedEvent(AuthUser authUser, LimitedEventCreateRequest request) {
        LimitedEvent event = LimitedEvent.builder()
                .bookId(request.getBookId())
                .title(request.getTitle())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .contents(request.getContents())
                .bookQuantity(request.getBookQuantity())
                .build();

        limitedEventRepository.save(event);

        return LimitedEventResponse.from(event);
    }

    /*
     * 단일 행사 조회
     */
    @Transactional(readOnly = true)
    public LimitedEventResponse getLimitedEvent(Long eventId) {
        LimitedEvent event = findByIdOrElseThrow(eventId);
        return LimitedEventResponse.from(event);
    }

    /*
     * 전체 행사 목록 조회
     */
    @Transactional(readOnly = true)
    public List<LimitedEventResponse> getAllLimitedEvents() {
        return limitedEventRepository.findAll().stream()
                .map(LimitedEventResponse::from)
                .toList();
    }

    /*
     * 한정판 행사 수정
     */
    @Transactional
    public LimitedEventResponse updateLimitedEvent(AuthUser authUser, Long eventId, LimitedEventUpdateRequest request) {
        LimitedEvent event = findByIdOrElseThrow(eventId);

        if (event.getStatus().equals(LimitedEventStatus.ACTIVE)) {
            throw new BadRequestException("이미 시작된 행사는 수정할 수 없습니다.");
        }

        event.update(request.getTitle(), request.getStartTime(), request.getEndTime(), request.getContents(), request.getBookQuantity());
        return LimitedEventResponse.from(event);
    }

    /*
     * 한정판 행사 삭제
     */
    @Transactional
    public void deleteLimitedEvent(AuthUser authUser, Long eventId) {
        LimitedEvent event = findByIdOrElseThrow(eventId);

        if (event.getStatus().equals(LimitedEventStatus.ACTIVE)) {
            throw new BadRequestException("이미 시작된 행사는 삭제할 수 없습니다.");
        }

        limitedEventRepository.delete(event);
    }

    /*
     * 내부용 find 메서드
     */
    private LimitedEvent findByIdOrElseThrow(Long eventId) {
        return limitedEventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_TOKEN.getMessage())
        );
    }
}
