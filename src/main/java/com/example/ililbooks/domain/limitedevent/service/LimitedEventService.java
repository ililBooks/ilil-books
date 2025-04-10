package com.example.ililbooks.domain.limitedevent.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.repository.BookRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class LimitedEventService {

    private final LimitedEventRepository limitedEventRepository;
    private final BookRepository bookRepository;

    /*
     * 한정판 행사 등록
     */
    @Transactional
    public LimitedEventResponse createLimitedEvent(AuthUser authUser, LimitedEventCreateRequest request) {
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BOOK.getMessage()));

        LimitedEvent limitedEvent = LimitedEvent.of(
                book,
                request.title(),
                request.startTime(),
                request.endTime(),
                request.contents(),
                request.bookQuantity()
        );

        limitedEventRepository.save(limitedEvent);

        return LimitedEventResponse.from(limitedEvent);
    }

    /*
     * 단일 행사 조회
     */
    @Transactional(readOnly = true)
    public LimitedEventResponse getLimitedEvent(Long limitedEventId) {
        LimitedEvent limitedEvent = findByIdOrElseThrow(limitedEventId);
        return LimitedEventResponse.from(limitedEvent);
    }

    /*
     * 전체 행사 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<LimitedEventResponse> getAllLimitedEvents(Pageable pageable) {
        Page<LimitedEvent> page = limitedEventRepository.findAll(pageable);
        List<LimitedEventResponse> filtered = page.getContent().stream()
                .filter(event -> !event.isDeleted())
                .map(LimitedEventResponse::from)
                .toList();

        return new PageImpl<>(filtered, pageable, filtered.size());
    }

    /*
     * 한정판 행사 수정
     */
    @Transactional
    public LimitedEventResponse updateLimitedEvent(AuthUser authUser, Long limitedEventId, LimitedEventUpdateRequest request) {
        LimitedEvent limitedEvent = findByIdOrElseThrow(limitedEventId);

        if (limitedEvent.getStatus() == LimitedEventStatus.ACTIVE) {
            // 시작된 행사인 경우 제한된 필드만 수정 가능
            limitedEvent.updateAfterStart(request);
        } else {
            limitedEvent.update(request.title(), request.startTime(), request.endTime(), request.contents(), request.bookQuantity());
        }

        return LimitedEventResponse.from(limitedEvent);
    }

    /*
     * 한정판 행사 삭제
     */
    @Transactional
    public void deleteLimitedEvent(AuthUser authUser, Long limitedEventId) {
        LimitedEvent limitedEvent = findByIdOrElseThrow(limitedEventId);

        if (limitedEvent.getStatus() == LimitedEventStatus.ACTIVE) {
            throw new BadRequestException(ALREADY_STARTED_EVENT_DELETE_NOT_ALLOWED.getMessage());
        }

        limitedEvent.softDelete();
    }

    /*
     * 내부용 find 메서드
     */
    private LimitedEvent findByIdOrElseThrow(Long limitedEventId) {
        return limitedEventRepository.findById(limitedEventId)
                .filter(event -> !event.isDeleted())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_TOKEN.getMessage()));
    }
}
