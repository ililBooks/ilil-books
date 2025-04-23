package com.example.ililbooks.domain.order.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import com.example.ililbooks.domain.limitedreservation.service.LimitedReservationReadService;
import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.repository.OrderRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.LoginType;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;

import static com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus.CANCELED;
import static com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus.SUCCESS;
import static com.example.ililbooks.global.exception.ErrorMessage.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderFromReservationServiceTest {

    @Mock
    private LimitedReservationReadService limitedReservationReadService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderHistoryService orderHistoryService;

    @InjectMocks
    private OrderService orderService;

    private final Pageable pageable = PageRequest.of(0, 10);

    /* createOrderFromReservation */
    @Test
    void 한정판_주문_생성_해당_유저의_예약이_아니어서_실패() {
        // Given
        Long userId = 1L;
        Long reservationId = 10L;
        AuthUser authUser = createAuthUser(userId);
        Users users = createTestUserWithId(2L);
        LimitedEvent limitedEvent = createTestEventWithId(1L, 5);
        LimitedReservation reservation = createTestReservationWithId(reservationId, users, limitedEvent, Instant.now().plusSeconds(3600), SUCCESS);

        given(limitedReservationReadService.findReservationByIdOrElseThrow(anyLong())).willReturn(reservation);

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> orderService.createOrderFromReservation(authUser, reservationId, pageable));
        assertEquals(badRequestException.getMessage(), NO_PERMISSION.getMessage());
    }

    @Test
    void 한정판_주문_생성_해당_예약이_성공하지_않으면_실패() {
        // Given
        Long userId = 1L;
        Long reservationId = 10L;
        AuthUser authUser = createAuthUser(userId);
        Users users = createTestUserWithId(userId);
        LimitedEvent limitedEvent = createTestEventWithId(1L, 5);
        LimitedReservation reservation = createTestReservationWithId(reservationId, users, limitedEvent, Instant.now().plusSeconds(3600), CANCELED);

        given(limitedReservationReadService.findReservationByIdOrElseThrow(anyLong())).willReturn(reservation);

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> orderService.createOrderFromReservation(authUser, reservationId, pageable));
        assertEquals(badRequestException.getMessage(), RESERVATION_NOT_SUCCESS.getMessage());
    }

    @Test
    void 한정판_주문_생성_예약_만료라_실패() {
        // Given
        Long userId = 1L;
        Long reservationId = 10L;
        AuthUser authUser = createAuthUser(userId);
        Users users = createTestUserWithId(userId);
        LimitedEvent limitedEvent = createTestEventWithId(1L, 5);
        LimitedReservation reservation = createTestReservationWithId(reservationId, users, limitedEvent, Instant.now().minusSeconds(3600), SUCCESS);

        given(limitedReservationReadService.findReservationByIdOrElseThrow(anyLong())).willReturn(reservation);

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> orderService.createOrderFromReservation(authUser, reservationId, pageable));
        assertEquals(badRequestException.getMessage(), RESERVATION_EXPIRED.getMessage());
    }

    @Test
    void 한정판_주문_생성_중복_주문_실패() {
        // given
        Long userId = 1L;
        Long reservationId = 10L;
        AuthUser authUser = createAuthUser(userId);
        Users users = createTestUserWithId(userId);
        LimitedEvent limitedEvent = createTestEventWithId(1L, 5);
        LimitedReservation reservation = createTestReservationWithId(reservationId, users, limitedEvent, Instant.now().plusSeconds(3600), SUCCESS);

        reservation.linkOrder(Order.builder()
                .id(1L)
                .users(Users.fromAuthUser(authUser))
                .number("order-number")
                .build());

        given(limitedReservationReadService.findReservationByIdOrElseThrow(anyLong())).willReturn(reservation);

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> orderService.createOrderFromReservation(authUser, reservationId, pageable));
        assertEquals(badRequestException.getMessage(), ALREADY_ORDERED.getMessage());
    }

    @Test
    void 한정판_주문_생성_성공() {
        // given
        Long userId = 1L;
        Long reservationId = 10L;
        AuthUser authUser = createAuthUser(userId);
        Users users = createTestUserWithId(userId);

        LimitedEvent limitedEvent = createTestEventWithId(1L, 100);
        LimitedReservation reservation = createTestReservationWithId(
                reservationId, users, limitedEvent, Instant.now().plusSeconds(3600), SUCCESS
        );

        given(limitedReservationReadService.findReservationByIdOrElseThrow(reservationId)).willReturn(reservation);
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        OrderResponse result = orderService.createOrderFromReservation(authUser, reservationId, pageable);

        // then
        assertNotNull(result);
        assertThat(result.totalPrice()).isEqualTo(new BigDecimal("20000"));
        verify(orderRepository).save(any(Order.class));
        verify(orderHistoryService).saveOrderHistory(anyMap(), any(Order.class));
    }

    // ---- 헬퍼 메서드 ----

    private AuthUser createAuthUser(Long id) {
        return AuthUser.builder()
                .userId(id)
                .email("user" + id + "@example.com")
                .nickname("유저" + id)
                .role(UserRole.ROLE_USER)
                .build();
    }

    private Users createTestUserWithId(Long id) {
        Users user = Users.of("user" + id + "@example.com", "유저" + id, "010-1111-1111", LoginType.EMAIL);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private LimitedEvent createTestEventWithId(Long id, int quantity) {
        Book book = Book.builder()
                .id(1L)
                .title("book1")
                .stock(100)
                .price(new BigDecimal(20000))
                .publisher("publisher1")
                .build();

        LimitedEvent event = LimitedEvent.of(
                book,
                "테스트 행사",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                "설명",
                quantity
        );
        ReflectionTestUtils.setField(event, "id", id);
        return event;
    }

    private LimitedReservation createTestReservationWithId(Long id, Users user, LimitedEvent event, Instant expiresAt, LimitedReservationStatus status) {
        LimitedReservation reservation = LimitedReservation.of(user, event, status, expiresAt);
        ReflectionTestUtils.setField(reservation, "id", id);
        return reservation;
    }
}