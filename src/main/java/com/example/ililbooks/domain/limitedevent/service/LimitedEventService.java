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
import com.example.ililbooks.global.exception.ErrorMessage;
import com.example.ililbooks.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.ililbooks.global.exception.ErrorMessage.NOT_FOUND_BOOK;
import static com.example.ililbooks.global.exception.ErrorMessage.NOT_FOUND_TOKEN;

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
        Book book = bookRepository.findById(request.getBookId()).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_BOOK.getMessage())
        );

        LimitedEvent limitedEvent = LimitedEvent.builder()
                .book(book)
                .title(request.getTitle())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .contents(request.getContents())
                .bookQuantity(request.getBookQuantity())
                .build();

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
        return limitedEventRepository.findAllByDeletedAtIsNull(pageable)
                .map(LimitedEventResponse::from);
    }

    /*
     * 한정판 행사 수정
     */
    //TODO delete, update authUser로 변경 권한 체크
    @Transactional
    public LimitedEventResponse updateLimitedEvent(AuthUser authUser, Long limitedEventId, LimitedEventUpdateRequest request) {
        LimitedEvent limitedEvent = findByIdOrElseThrow(limitedEventId);

        if (limitedEvent.getStatus() == LimitedEventStatus.ACTIVE) {
            // 시작된 행사인 경우 제한된 필드만 수정 가능
            limitedEvent.updateAfterStart(request);
        } else {
            // 아직 시작 안한 행사면 전체 필드 수정 가능
            limitedEvent.update(request.getTitle(), request.getStartTime(), request.getEndTime(), request.getContents(), request.getBookQuantity());
        }

        return LimitedEventResponse.from(limitedEvent);
    }

    /*
     * 한정판 행사 삭제
     */
    @Transactional
    public void deleteLimitedEvent(AuthUser authUser, Long limitedEventId) {
        LimitedEvent limitedEvent = findByIdOrElseThrow(limitedEventId);

        if (limitedEvent.getStatus().equals(LimitedEventStatus.ACTIVE)) {
            throw new BadRequestException(ErrorMessage.ALREADY_STARTED_EVENT_DELETE_NOT_ALLOWED.getMessage());
        }

        limitedEvent.softDelete();
    }

    /*
     * 내부용 find 메서드
     */
    private LimitedEvent findByIdOrElseThrow(Long limitedEventId) {
        return limitedEventRepository.findByIdAndDeletedAtIsNull(limitedEventId).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_TOKEN.getMessage())
        );
    }
}
