package com.example.ililbooks.domain.limitedevent.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.repository.BookRepository;
import com.example.ililbooks.domain.limitedevent.dto.request.LimitedEventCreateRequest;
import com.example.ililbooks.domain.limitedevent.dto.request.LimitedEventUpdateRequest;
import com.example.ililbooks.domain.limitedevent.dto.response.LimitedEventResponse;
import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedevent.enums.LimitedEventStatus;
import com.example.ililbooks.domain.limitedevent.repository.LimitedEventRepository;
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
     * 한정판 행사 등록 (책 등록자만 가능-출판사)
     */
    @Transactional
    public LimitedEventResponse createLimitedEvent(Long userId, LimitedEventCreateRequest request) {
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BOOK.getMessage()));

        if (!book.getUsers().getId().equals(userId)) {
            throw new BadRequestException(NO_PERMISSION.getMessage());
        }

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

        return new PageImpl<>(filtered, pageable, page.getTotalElements());
    }

    /*
     * 한정판 행사 수정
     */
    @Transactional
    public LimitedEventResponse updateLimitedEvent(Long userId, Long limitedEventId, LimitedEventUpdateRequest request) {
        LimitedEvent limitedEvent = findByIdOrElseThrow(limitedEventId);

        if (!limitedEvent.getBook().getUsers().getId().equals(userId)) {
            throw new BadRequestException(NO_PERMISSION.getMessage());
        }

        if (limitedEvent.getStatus() == LimitedEventStatus.ACTIVE) {
            limitedEvent.updateAfterStart(request);
        } else {
            limitedEvent.update(
                    request.title(),
                    request.startTime(),
                    request.endTime(),
                    request.contents(),
                    request.bookQuantity()
            );
        }

        return LimitedEventResponse.from(limitedEvent);
    }

    /*
     * 한정판 행사 삭제
     */
    @Transactional
    public void deleteLimitedEvent(Long userId, Long limitedEventId) {
        LimitedEvent limitedEvent = findByIdOrElseThrow(limitedEventId);

        if (!limitedEvent.getBook().getUsers().getId().equals(userId)) {
            throw new BadRequestException(NO_PERMISSION.getMessage());
        }

        if (limitedEvent.getStatus() == LimitedEventStatus.ACTIVE) {
            throw new BadRequestException(ALREADY_STARTED_EVENT_DELETE_NOT_ALLOWED.getMessage());
        }

        limitedEvent.softDelete();
    }

    /*
     * 내부용 find 메서드
     */
    private LimitedEvent findByIdOrElseThrow(Long id) {
        return limitedEventRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_EVENT.getMessage()));
    }
}
