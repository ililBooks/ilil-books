package com.example.ililbooks.domain.limitedreservation.service;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import com.example.ililbooks.domain.limitedreservation.repository.LimitedReservationRepository;
import com.example.ililbooks.domain.order.dto.response.OrdersGetResponse;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.LoginType;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus.SUCCESS;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class LimitedReservationOrderServiceTest {
//
//    @Mock
//    private LimitedReservationRepository limitedReservationRepository;
//
//    @Mock
//    private UserService userService;
//
//    @InjectMocks
//    private LimitedReservationOrderService limitedReservationOrderService;
//
//    @Test
//    void 주문_생성_성공() {
//        // Given
//        Long userId = 1L;
//        Long reservationId = 10L;
//        Users user = createTestUserWithId(userId);
//        LimitedEvent limitedEvent = createTestEventWithId(1L, 5);
//        LimitedReservation reservation = createTestReservationWithId(reservationId, user, limitedEvent, Instant.now().plusSeconds(3600), SUCCESS);
//        AuthUser authUser = createAuthUser(userId);
//
//        given(limitedReservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
//        given(userService.findByIdOrElseThrow(userId)).willReturn(user);
//
//        // When
//        OrdersGetResponse response = limitedReservationOrderService.createOrderFromReservation(authUser, reservationId);
//
//        // Then
//        assertThat(response).isNotNull();
//        assertThat(response.totalPrice()).isEqualTo(limitedEvent.getBook().getPrice());
//    }
//
//    @Test
//    void 주문_실패_만료() {
//        // Given
//        Long userId = 2L;
//        Long reservationId = 20L;
//        Users user = createTestUserWithId(userId);
//        LimitedEvent limitedEvent = createTestEventWithId(2L, 5);
//        LimitedReservation reservation = createTestReservationWithId(reservationId, user, limitedEvent, Instant.now().minusSeconds(1), SUCCESS);
//        AuthUser authUser = createAuthUser(userId);
//
//        given(limitedReservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
//
//        // When & Then
//        assertThatThrownBy(() -> limitedReservationOrderService.createOrderFromReservation(authUser, reservationId))
//                .isInstanceOf(BadRequestException.class)
//                .hasMessageContaining("예약시간이 만료되었습니다.");
//    }
//
//    @Test
//    void 주문_중복_실패() {
//        // Given
//        Long userId = 3L;
//        Long reservationId = 30L;
//        Users user = createTestUserWithId(userId);
//        LimitedEvent limitedEvent = createTestEventWithId(3L, 5);
//        LimitedReservation reservation = createTestReservationWithId(reservationId, user, limitedEvent, Instant.now().plusSeconds(3600), SUCCESS);
//        reservation.linkOrder(mock(com.example.ililbooks.domain.order.entity.Order.class));
//        AuthUser authUser = createAuthUser(userId);
//
//        given(limitedReservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
//
//        // When & Then
//        assertThatThrownBy(() -> limitedReservationOrderService.createOrderFromReservation(authUser, reservationId))
//                .isInstanceOf(BadRequestException.class)
//                .hasMessageContaining("이미 예약되었습니다.");
//    }
//
//    @Test
//    void 재고부족_주문_실패() {
//        // Given
//        Long userId = 4L;
//        Long reservationId = 40L;
//        Users user = createTestUserWithId(userId);
//        LimitedEvent limitedEvent = createTestEventWithId(4L, 0); // 재고 0
//        LimitedReservation reservation = createTestReservationWithId(reservationId, user, limitedEvent, Instant.now().plusSeconds(3600), SUCCESS);
//        AuthUser authUser = createAuthUser(userId);
//
//        given(limitedReservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
//
//        // When & Then
//        assertThatThrownBy(() -> limitedReservationOrderService.createOrderFromReservation(authUser, reservationId))
//                .isInstanceOf(BadRequestException.class)
//                .hasMessageContaining("재고가 부족합니다.");
//    }
//
//    @Test
//    void 주문_실패_예약상태() {
//        // Given
//        Long userId = 5L;
//        Long reservationId = 50L;
//        Users user = createTestUserWithId(userId);
//        LimitedEvent limitedEvent = createTestEventWithId(5L, 1);
//        LimitedReservation reservation = createTestReservationWithId(reservationId, user, limitedEvent, Instant.now().plusSeconds(3600), LimitedReservationStatus.WAITING);
//        AuthUser authUser = createAuthUser(userId);
//
//        given(limitedReservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
//
//        // When & Then
//        assertThatThrownBy(() -> limitedReservationOrderService.createOrderFromReservation(authUser, reservationId))
//                .isInstanceOf(BadRequestException.class)
//                .hasMessageContaining("예약이 성공하지 못했습니다.");
//    }
//
//    @Test
//    void 사용자_불일치_주문_실패() {
//        // Given
//        Long userId = 6L;
//        Long reservationId = 60L;
//        Users user = createTestUserWithId(999L);
//        LimitedEvent limitedEvent = createTestEventWithId(6L, 1);
//        LimitedReservation reservation = createTestReservationWithId(reservationId, user, limitedEvent, Instant.now().plusSeconds(3600), SUCCESS);
//        AuthUser authUser = createAuthUser(userId);
//
//        given(limitedReservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
//
//        // When & Then
//        assertThatThrownBy(() -> limitedReservationOrderService.createOrderFromReservation(authUser, reservationId))
//                .isInstanceOf(BadRequestException.class)
//                .hasMessageContaining("권한이 없습니다");
//    }
//
//    // ---- 헬퍼 메서드 ----
//
//    private AuthUser createAuthUser(Long id) {
//        return AuthUser.builder()
//                .userId(id)
//                .email("user" + id + "@example.com")
//                .nickname("유저" + id)
//                .role(UserRole.ROLE_USER)
//                .build();
//    }
//
//    private Users createTestUserWithId(Long id) {
//        Users user = Users.of("user" + id + "@example.com", "유저" + id, "010-1111-1111", LoginType.EMAIL);
//        ReflectionTestUtils.setField(user, "id", id);
//        return user;
//    }
//
//    private LimitedEvent createTestEventWithId(Long id, int quantity) {
//        LimitedEvent event = LimitedEvent.of(
//                mock(com.example.ililbooks.domain.book.entity.Book.class),
//                "테스트 행사",
//                Instant.now(),
//                Instant.now().plusSeconds(3600),
//                "설명",
//                quantity
//        );
//        ReflectionTestUtils.setField(event, "id", id);
//        return event;
//    }
//
//    private LimitedReservation createTestReservationWithId(Long id, Users user, LimitedEvent event, Instant expiresAt, LimitedReservationStatus status) {
//        LimitedReservation reservation = LimitedReservation.of(user, event, status, expiresAt);
//        ReflectionTestUtils.setField(reservation, "id", id);
//        return reservation;
//    }
//}