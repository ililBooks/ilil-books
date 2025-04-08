package com.example.ililbooks.domain.limitedevent.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.repository.BookRepository;
import com.example.ililbooks.domain.limitedevent.dto.request.LimitedEventCreateRequest;
import com.example.ililbooks.domain.limitedevent.dto.request.LimitedEventUpdateRequest;
import com.example.ililbooks.domain.limitedevent.dto.response.LimitedEventResponse;
import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedevent.repository.LimitedEventRepository;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LimitedEventServiceTest {

    @Mock
    private LimitedEventRepository limitedEventRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private LimitedEventService limitedEventService;

    private static final Long TEST_LIMITED_EVENT_ID = 1L;
    private static final AuthUser TEST_AUTH_USER = new AuthUser(1L, "test@sample.com", "닉네임", UserRole.ROLE_PUBLISHER);

    @Test
    void 한정판_행사_등록성공() {
        // Given
        LimitedEventCreateRequest request = new LimitedEventCreateRequest(1L, "이벤트제목", nowPlus(1), nowPlus(7), "행사내용", 100);

        Book mockBook = Book.builder()
                .title("책 제목")
                .author("작가")
                .price(15000L)
                .category("카테고리")
                .stock(50)
                .build();

        given(bookRepository.findById(1L)).willReturn(Optional.of(mockBook));

        // When
        LimitedEventResponse response = limitedEventService.createLimitedEvent(TEST_AUTH_USER, request);

        // Then
        assertNotNull(response);
        verify(limitedEventRepository, times(1)).save(any(LimitedEvent.class));
    }

    @Test
    void 행사_단건_조회성공() {
        // Given
        LimitedEvent limitedEvent = createEvent();

        given(limitedEventRepository.findByIdAndDeletedAtIsNull(TEST_LIMITED_EVENT_ID)).willReturn(Optional.of(limitedEvent));

        // When
        LimitedEventResponse response = limitedEventService.getLimitedEvent(TEST_LIMITED_EVENT_ID);

        // Then
        assertThat(response.getTitle()).isEqualTo("행사이름");
    }

    @Test
    void 존재하지_않는_행사_조회() {
        // Given
        given(limitedEventRepository.findByIdAndDeletedAtIsNull(TEST_LIMITED_EVENT_ID)).willReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> limitedEventService.getLimitedEvent(TEST_LIMITED_EVENT_ID));
    }

    @Test
    void 행사_전체_조회성공() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<LimitedEvent> eventPage = new PageImpl<>(List.of(createEvent(), createEvent(), createEvent()));

        given(limitedEventRepository.findAllByDeletedAtIsNull(pageable)).willReturn(eventPage);

        // When
        Page<LimitedEventResponse> result = limitedEventService.getAllLimitedEvents(pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void 행사_수정성공() {
        // Given
        LimitedEvent limitedEvent = createEvent();
        LimitedEventUpdateRequest request = new LimitedEventUpdateRequest("수정된제목", nowPlus(3), nowPlus(14), "수정된내용", 150);

        given(limitedEventRepository.findByIdAndDeletedAtIsNull(TEST_LIMITED_EVENT_ID)).willReturn(Optional.of(limitedEvent));

        // When
        LimitedEventResponse response = limitedEventService.updateLimitedEvent(TEST_AUTH_USER, TEST_LIMITED_EVENT_ID, request);

        // Then
        assertEquals("수정된제목", response.getTitle());
        assertEquals(150, response.getBookQuantity());
    }

    @Test
    void ACTIVE_상태에서_일부필드_수정성공() {
        // Given
        LimitedEvent limitedEvent = createEvent();
        limitedEvent.activate();
        // 제한된 필드만 수정
        LimitedEventUpdateRequest request = new LimitedEventUpdateRequest(null, null, nowPlus(30), null, 300);

        given(limitedEventRepository.findByIdAndDeletedAtIsNull(TEST_LIMITED_EVENT_ID)).willReturn(Optional.of(limitedEvent));

        // When
        LimitedEventResponse response = limitedEventService.updateLimitedEvent(TEST_AUTH_USER, TEST_LIMITED_EVENT_ID, request);

        // Then
        LocalDateTime expectedEndTime = nowPlus(30);
        assertThat(response.getBookQuantity()).isEqualTo(300);
    }

    @Test
    void 행사_삭제성공() {
        // Given
        LimitedEvent limitedEvent = createEvent();

        given(limitedEventRepository.findByIdAndDeletedAtIsNull(TEST_LIMITED_EVENT_ID)).willReturn(Optional.of(limitedEvent));

        // When
        limitedEventService.deleteLimitedEvent(TEST_AUTH_USER, TEST_LIMITED_EVENT_ID);

        // Then
        assertNotNull(limitedEvent.getDeletedAt());
    }

    @Test
    void ACTIVE_상태로_행사_삭제실패() {
        // Given
        LimitedEvent limitedEvent = createEvent();
        limitedEvent.activate();

        given(limitedEventRepository.findByIdAndDeletedAtIsNull(TEST_LIMITED_EVENT_ID)).willReturn(Optional.of(limitedEvent));

        // When & Then
        assertThrows(BadRequestException.class, () -> limitedEventService.deleteLimitedEvent(TEST_AUTH_USER, TEST_LIMITED_EVENT_ID));
    }

    // 헬퍼 메서드
    private LimitedEvent createEvent() {
        Book mockBook = Book.builder()
                .title("책 제목")
                .author("작가")
                .price(10000L)
                .category("카테고리")
                .stock(50)
                .build();

        return LimitedEvent.builder()
                .book(mockBook)
                .title("행사이름")
                .startTime(nowPlus(1))
                .endTime(nowPlus(7))
                .contents("행사내용")
                .bookQuantity(100)
                .build();
    }

    private LocalDateTime nowPlus(int days) {
        return LocalDateTime.now().plusDays(days);
    }
}