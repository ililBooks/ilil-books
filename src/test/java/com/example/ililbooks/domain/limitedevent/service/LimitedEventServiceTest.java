package com.example.ililbooks.domain.limitedevent.service;

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LimitedEventServiceTest {

    @Mock
    private LimitedEventRepository limitedEventRepository;

    @InjectMocks
    private LimitedEventService limitedEventService;

    @Test
    void 항정판_행사_등록성공() {
        // Given
        AuthUser authUser = new AuthUser(1L, "test@email.com", "닉네임", UserRole.ROLE_PUBLISHER);
        LimitedEventCreateRequest request = new LimitedEventCreateRequest(1L, "이벤트제목", nowPlus(1), nowPlus(7), "행사내용", 100);

        // When
        LimitedEventResponse response = limitedEventService.createLimitedEvent(authUser, request);

        // Then
        assertNotNull(response);
        verify(limitedEventRepository, times(1)).save(any(LimitedEvent.class));
    }

    @Test
    void 행사_조회성공() {
        // Given
        Long limitedEventId = 1L;
        LimitedEvent limitedEvent = createEvent();

        given(limitedEventRepository.findById(limitedEventId)).willReturn(Optional.of(limitedEvent));

        // When
        LimitedEventResponse response = limitedEventService.getLimitedEvent(limitedEventId);

        // Then
        assertEquals("행사이름", response.getTitle());
    }

    @Test
    void 존재하지_않는_행사_조회() {
        // Given
        Long limitedEventId = 1L;

        given(limitedEventRepository.findById(limitedEventId)).willReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> limitedEventService.getLimitedEvent(limitedEventId));
    }

    @Test
    void 행사_전체_조회성공() {
        // Given
        List<LimitedEvent> limitedEvents = List.of(createEvent(), createEvent(), createEvent());

        given(limitedEventRepository.findAll()).willReturn(limitedEvents);

        // When
        List<LimitedEventResponse> result = limitedEventService.getAllLimitedEvents();

        // Then
        assertEquals(3, result.size());
    }

    @Test
    void 행사_수정성공() {
        // Given
        Long limitedEventId = 1L;
        AuthUser authUser = new AuthUser(1L, "test@email.com", "닉네임", UserRole.ROLE_PUBLISHER);
        LimitedEvent limitedEvent = createEvent();
        LimitedEventUpdateRequest request = new LimitedEventUpdateRequest("수정된제목", nowPlus(3), nowPlus(14), "수정된내용", 150);

        given(limitedEventRepository.findById(limitedEventId)).willReturn(Optional.of(limitedEvent));

        // When
        LimitedEventResponse response = limitedEventService.updateLimitedEvent(authUser, limitedEventId, request);

        // Then
        assertEquals("수정된제목", response.getTitle());
        assertEquals(150, response.getBookQuantity());
    }

    @Test
    void ACTIVE_상태로_행사_수정실패() {
        // Given
        Long limitedEventId = 1L;
        AuthUser authUser = new AuthUser(1L, "test@email.com", "닉네임", UserRole.ROLE_PUBLISHER);
        LimitedEvent limitedEvent = createEvent();
        limitedEvent.activate();
        LimitedEventUpdateRequest request = new LimitedEventUpdateRequest("수정된제목", nowPlus(3), nowPlus(14), "수정된내용", 150);

        given(limitedEventRepository.findById(limitedEventId)).willReturn(Optional.of(limitedEvent));

        // When & Then
        assertThrows(BadRequestException.class, () -> limitedEventService.updateLimitedEvent(authUser, limitedEventId, request));
    }

    @Test
    void 행사_삭제성공() {
        // Given
        Long limitedEventId = 1L;
        AuthUser authUser = new AuthUser(1L, "test@email.com", "닉네임", UserRole.ROLE_PUBLISHER);
        LimitedEvent limitedEvent = createEvent();

        given(limitedEventRepository.findById(limitedEventId)).willReturn(Optional.of(limitedEvent));

        // When
        limitedEventService.deleteLimitedEvent(authUser, limitedEventId);

        // Then
        verify(limitedEventRepository, times(1)).delete(limitedEvent);
    }

    @Test
    void ACTIVE_상태로_행사_삭제실패() {
        // Given
        Long limitedEventId = 1L;
        AuthUser authUser = new AuthUser(1L, "test@email.com", "닉네임", UserRole.ROLE_PUBLISHER);
        LimitedEvent limitedEvent = createEvent();
        limitedEvent.activate();

        given(limitedEventRepository.findById(limitedEventId)).willReturn(Optional.of(limitedEvent));

        // When & Then
        assertThrows(BadRequestException.class, () -> limitedEventService.deleteLimitedEvent(authUser, limitedEventId));
    }

    // 헬퍼 메서드
    private LimitedEvent createEvent() {
        return LimitedEvent.builder()
                .bookId(1L)
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