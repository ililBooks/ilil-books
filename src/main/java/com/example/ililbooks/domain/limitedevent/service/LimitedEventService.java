package com.example.ililbooks.domain.limitedevent.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.service.BookService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LimitedEventService {

    private final LimitedEventRepository limitedEventRepository;
    private final BookService bookService;

    /*
     * 한정판 행사 등록 (책 등록자만 가능-출판사)
     */
    @Transactional
    public LimitedEventResponse createLimitedEvent(AuthUser authUser, LimitedEventCreateRequest request) {
        Book book = bookService.findBookByIdOrElseThrow(request.bookId());

        validateOwnership(authUser.getUserId(), book);

        LimitedEvent limitedEvent = LimitedEvent.of(
                book,
                request.title(),
                request.startTime(),
                request.endTime(),
                request.contents(),
                request.bookQuantity()
        );

        limitedEventRepository.save(limitedEvent);
        log.info("[한정판 행사 생성] userId={}, bookId={}, eventId={}", authUser.getUserId(), book.getId(), limitedEvent.getId());

        return LimitedEventResponse.of(limitedEvent);
    }

    /*
     * 단일 행사 조회
     */
    @Transactional(readOnly = true)
    public LimitedEventResponse getLimitedEvent(Long limitedEventId) {
        LimitedEvent limitedEvent = findByIdWithBookAndUserOrElseThrow(limitedEventId);
        return LimitedEventResponse.of(limitedEvent);
    }

    /*
     * 전체 행사 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<LimitedEventResponse> getAllLimitedEvents(Pageable pageable) {
        return limitedEventRepository.findAllByIsDeletedFalse(pageable)
                .map(LimitedEventResponse::of);
    }

    /*
     * 한정판 행사 수정
     */
    @Transactional
    public LimitedEventResponse updateLimitedEvent(AuthUser authUser, Long limitedEventId, LimitedEventUpdateRequest request) {
        LimitedEvent limitedEvent = findByIdWithBookAndUserOrElseThrow(limitedEventId);
        validateOwnership(authUser.getUserId(), limitedEvent.getBook());

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
        log.info("[한정판 행사 수정] eventId={}, userId={}", limitedEvent.getId(), authUser.getUserId());

        return LimitedEventResponse.of(limitedEvent);
    }

    /*
     * 한정판 행사 삭제
     */
    @Transactional
    public void deleteLimitedEvent(AuthUser authUser, Long limitedEventId) {
        LimitedEvent limitedEvent = findByIdWithBookAndUserOrElseThrow(limitedEventId);
        validateOwnership(authUser.getUserId(), limitedEvent.getBook());

        if (limitedEvent.getStatus() == LimitedEventStatus.ACTIVE) {
            throw new BadRequestException(ALREADY_STARTED_EVENT_DELETE_NOT_ALLOWED.getMessage());
        }

        limitedEvent.softDelete();

        log.info("[한정판 행사 삭제] eventId={}, userId={}", limitedEvent.getId(), authUser.getUserId());
    }

    // ---- 내부 메서드 ----

    private void validateOwnership(Long userId, Book book) {
        if (!book.getUsers().getId().equals(userId)) {
            throw new BadRequestException(NO_PERMISSION.getMessage());
        }
    }

    private LimitedEvent findByIdWithBookAndUserOrElseThrow(Long id) {
        return limitedEventRepository.findByIdWithBookAndUser(id).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_EVENT.getMessage()));
    }
}
