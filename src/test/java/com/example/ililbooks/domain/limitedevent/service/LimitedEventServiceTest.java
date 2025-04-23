//package com.example.ililbooks.domain.limitedevent.service;
//
//import com.example.ililbooks.domain.book.entity.Book;
//import com.example.ililbooks.domain.book.repository.BookRepository;
//import com.example.ililbooks.domain.limitedevent.dto.request.LimitedEventCreateRequest;
//import com.example.ililbooks.domain.limitedevent.dto.response.LimitedEventResponse;
//import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
//import com.example.ililbooks.domain.user.entity.Users;
//import com.example.ililbooks.domain.user.enums.LoginType;
//import com.example.ililbooks.domain.user.enums.UserRole;
//import com.example.ililbooks.domain.limitedevent.repository.LimitedEventRepository;
//import com.example.ililbooks.global.dto.AuthUser;
//import com.example.ililbooks.global.exception.BadRequestException;
//import com.example.ililbooks.global.exception.NotFoundException;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.BDDMockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class LimitedEventServiceTest {
//
//    @Mock
//    private LimitedEventRepository limitedEventRepository;
//
//    @Mock
//    private BookRepository bookRepository;
//
//    @InjectMocks
//    private LimitedEventService limitedEventService;
//
//    @Test
//    void 한정판행사_등록성공() {
//        // Given
//        Long userId= 1L;
//        Long bookId = 100L;
//        AuthUser authUser = createAuthUser(userId);
//        Users user = createTestUser(userId);
//        Book book = createTestBook(bookId, user);
//        LimitedEventCreateRequest request = new LimitedEventCreateRequest(bookId, "한정판 행사", Instant.now().plusSeconds(60), Instant.now().plusSeconds(3600), "행사내용", 50);
//
//        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
//
//        // When
//        LimitedEventResponse response = limitedEventService.createLimitedEvent(authUser, request);
//
//        // Then
//        assertThat(response).isNotNull();
//        assertThat(response.title()).isEqualTo("한정판 행사");
//        then(limitedEventRepository).should().save(any(LimitedEvent.class));
//    }
//
//    @Test
//    void 한정판행사_등록실패_권한없음() {
//        // Given
//        Long userId = 1L;
//        Long bookId = 100L;
//        Users otherUser = createTestUser(2L);
//        Book book = createTestBook(bookId, otherUser);
//        AuthUser authUser = createAuthUser(userId);
//        LimitedEventCreateRequest request = new LimitedEventCreateRequest(bookId, "한정판 행사", Instant.now().plusSeconds(60), Instant.now().plusSeconds(3600), "행사내용", 50);
//
//        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
//
//        // When & Then
//        assertThatThrownBy(() -> limitedEventService.createLimitedEvent(authUser, request))
//                .isInstanceOf(BadRequestException.class)
//                .hasMessageContaining("권한이 없습니다.");
//    }
//
//    @Test
//    void 한정판행사_등록실패_책없음() {
//        // Given
//        Long bookId = 200L;
//        AuthUser authUser =createAuthUser(1L);
//        LimitedEventCreateRequest request = new LimitedEventCreateRequest(bookId, "한정판 행사", Instant.now().plusSeconds(60), Instant.now().plusSeconds(3600), "행사내용", 50);
//
//        given(bookRepository.findById(bookId)).willReturn(Optional.empty());
//
//        // When & Then
//        assertThatThrownBy(() -> limitedEventService.createLimitedEvent(authUser, request))
//                .isInstanceOf(NotFoundException.class)
//                .hasMessageContaining("책을 찾을 수 없습니다.");
//    }
//
//    // ----- 헬퍼 메서드 -----
//
//    private AuthUser createAuthUser(Long id) {
//        return AuthUser.builder()
//                .userId(id)
//                .email("user" + id + "@example.com")
//                .nickname("user" + id)
//                .role(UserRole.ROLE_PUBLISHER)
//                .build();
//    }
//
//    private Users createTestUser(Long id) {
//        Users user = Users.of("user" + id + "@example.com", "user" + id, "010-0000-0000", LoginType.EMAIL);
//        ReflectionTestUtils.setField(user, "id", id);
//        return user;
//    }
//
//    private Book createTestBook(Long bookId, Users user) {
//        Book book = Book.of(user, "책 제목", "저자", BigDecimal.valueOf(10000), "소설", 100, "ISBN123", "출판사");
//        ReflectionTestUtils.setField(book, "id", bookId);
//        return book;
//    }
//}