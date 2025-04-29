package com.example.ililbooks.domain.limitedevent.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.enums.LimitedType;
import com.example.ililbooks.domain.book.service.BookService;
import com.example.ililbooks.domain.limitedevent.dto.request.LimitedEventCreateRequest;
import com.example.ililbooks.domain.limitedevent.dto.request.LimitedEventUpdateRequest;
import com.example.ililbooks.domain.limitedevent.dto.response.LimitedEventResponse;
import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedevent.repository.LimitedEventRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class LimitedEventServiceTest {

    @Mock
    private BookService bookService;

    @Mock
    private LimitedEventRepository limitedEventRepository;

    @InjectMocks
    private LimitedEventService limitedEventService;

    private Users admin;
    private Book limitedBook;
    private AuthUser authUser;
    private LimitedEventCreateRequest createRequest;
    private LimitedEventUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        admin = Users.builder()
                .id(1L)
                .nickname("관리자")
                .userRole(UserRole.ROLE_ADMIN)
                .build();

        limitedBook = Book.builder()
                .id(1L)
                .title("한정판 책")
                .limitedType(LimitedType.LIMITED)
                .users(admin)
                .build();

        authUser = AuthUser.builder()
                .userId(1L)
                .email("admin@example.com")
                .nickname("관리자")
                .role(UserRole.ROLE_ADMIN)
                .build();

        createRequest = LimitedEventCreateRequest.builder()
                .bookId(1L)
                .title("한정 이벤트")
                .startTime(Instant.now().plusSeconds(3600))
                .endTime(Instant.now().plusSeconds(7200))
                .contents("한정 내용")
                .bookQuantity(10)
                .build();

        updateRequest = LimitedEventUpdateRequest.builder()
                .title("수정된 제목")
                .startTime(Instant.now().plusSeconds(7200))
                .endTime(Instant.now().plusSeconds(10800))
                .contents("수정된 내용")
                .bookQuantity(5)
                .build();
    }

    @Test
    void 한정판_행사_생성_성공() {
        // Given
        given(bookService.findBookByIdOrElseThrow(1L)).willReturn(limitedBook);
        given(limitedEventRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        // When
        LimitedEventResponse response = limitedEventService.createLimitedEvent(authUser, createRequest);

        // Then
        assertThat(response.title()).isEqualTo(createRequest.title());
        verify(limitedEventRepository).save(any());
    }

    @Test
    void 한정판_행사_조회_성공() {
        // given
        LimitedEvent limitedEvent = LimitedEvent.of(limitedBook, "제목", Instant.now(), Instant.now().plusSeconds(3600), "내용", 10);
        given(limitedEventRepository.findByIdWithBookAndUser(anyLong())).willReturn(Optional.of(limitedEvent));

        // when
        LimitedEventResponse result = limitedEventService.getLimitedEvent(1L);

        // then
        assertThat(result.title()).isEqualTo("제목");
        verify(limitedEventRepository).findByIdWithBookAndUser(1L);
    }

    @Test
    void 존재하지_않는_행사_조회_실패() {
        // given
        given(limitedEventRepository.findByIdWithBookAndUser(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> limitedEventService.getLimitedEvent(1L));
    }

    @Test
    void 한정판_행사_전체_조회() {
        // given
        Pageable pageable = PageRequest.of(0, 5);
        LimitedEvent limitedEvent = LimitedEvent.of(limitedBook, "이벤트", Instant.now(), Instant.now().plusSeconds(3600), "내용", 10);
        given(limitedEventRepository.findAllByIsDeletedFalse(pageable)).willReturn(new PageImpl<>(List.of(limitedEvent)));

        // when
        var result = limitedEventService.getAllLimitedEvents(pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(limitedEventRepository).findAllByIsDeletedFalse(pageable);
    }

    @Test
    void 한정판_행사_수정_성공() {
        // given
        LimitedEvent limitedEvent = LimitedEvent.of(limitedBook, "기존제목", Instant.now(), Instant.now().plusSeconds(3600), "내용", 10);
        given(limitedEventRepository.findByIdWithBookAndUser(1L)).willReturn(Optional.of(limitedEvent));

        // when
        LimitedEventResponse response = limitedEventService.updateLimitedEvent(authUser, 1L, updateRequest);

        // then
        assertThat(response.title()).isEqualTo("수정된 제목");
    }

    @Test
    void 한정판_행사_삭제_성공() {
        // given
        LimitedEvent limitedEvent = LimitedEvent.of(limitedBook, "제목", Instant.now(), Instant.now().plusSeconds(3600), "내용", 10);
        given(limitedEventRepository.findByIdWithBookAndUser(1L)).willReturn(Optional.of(limitedEvent));

        // when
        limitedEventService.deleteLimitedEvent(authUser, 1L);

        // then
        assertThat(limitedEvent.isDeleted()).isTrue();
    }

    @Test
    void 활성화_상태인_행사_삭제_실패() {
        // given
        LimitedEvent limitedEvent = LimitedEvent.of(limitedBook, "제목", Instant.now(), Instant.now().plusSeconds(3600), "내용", 10);
        limitedEvent.activate();
        given(limitedEventRepository.findByIdWithBookAndUser(1L)).willReturn(Optional.of(limitedEvent));

        // when & then
        assertThrows(BadRequestException.class, () -> limitedEventService.deleteLimitedEvent(authUser, 1L));
    }
}