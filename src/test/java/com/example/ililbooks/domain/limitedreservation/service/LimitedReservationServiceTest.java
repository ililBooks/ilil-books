package com.example.ililbooks.domain.limitedreservation.service;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedevent.repository.LimitedEventRepository;
import com.example.ililbooks.domain.limitedreservation.dto.request.LimitedReservationCreateRequest;
import com.example.ililbooks.domain.limitedreservation.dto.response.LimitedReservationResponse;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.repository.LimitedReservationRepository;
import com.example.ililbooks.domain.limitedreservation.repository.LimitedReservationStatusHistoryRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.global.redis.lock.RedissonLockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class LimitedReservationServiceTest {

    @Mock
    private LimitedReservationRepository reservationRepository;

    @Mock
    private LimitedEventRepository eventRepository;

    @Mock
    private UserService userService;

    @Mock
    private LimitedReservationQueueService queueService;

    @Mock
    private LimitedReservationExpireQueueService expireQueueService;

    @Mock
    private RedissonLockService redissonLockService;

    @Mock
    private LimitedReservationStatusHistoryRepository historyRepository;

    @InjectMocks
    private LimitedReservationService reservationService;

    private AuthUser authUser;
    private Users user;
    private LimitedEvent event;
    private LimitedReservationCreateRequest request;

    @BeforeEach
    void setUp() {
        authUser = AuthUser.builder()
                .userId(1L)
                .email("test@test.com")
                .nickname("관리자")
                .role(UserRole.ROLE_ADMIN)
                .build();

        user = mock(Users.class);
        event = mock(LimitedEvent.class);

        request = LimitedReservationCreateRequest.builder()
                .limitedEventId(1L)
                .build();
    }

    @Test
    void 예약_생성_성공_예약가능() {
        // Given
        given(eventRepository.findById(anyLong())).willReturn(Optional.of(event));
        given(reservationRepository.findByUsersIdAndLimitedEvent(anyLong(), any())).willReturn(Optional.empty());
        given(userService.findByIdOrElseThrow(anyLong())).willReturn(user);
        given(reservationRepository.countByLimitedEventAndStatus(event, RESERVED)).willReturn(0L);
        given(event.canAcceptReservation(0L)).willReturn(true);
        given(reservationRepository.save(any())).willAnswer(invocation -> {
            LimitedReservation r = invocation.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 10L);
            return r;
        });

        // When
        LimitedReservationResponse result = reservationService.createReservationWithLock(authUser, request);

        // Then
        assertThat(result.status()).isEqualTo(RESERVED);
        verify(expireQueueService).addToExpireQueue(anyLong(), anyLong(), any());
    }

    @Test
    void 예약_생성_성공_WAITING() {
        // Given
        given(eventRepository.findById(anyLong())).willReturn(Optional.of(event));
        given(reservationRepository.findByUsersIdAndLimitedEvent(anyLong(), any())).willReturn(Optional.empty());
        given(userService.findByIdOrElseThrow(anyLong())).willReturn(user);
        given(reservationRepository.countByLimitedEventAndStatus(event, RESERVED)).willReturn(0L);
        given(event.canAcceptReservation(0L)).willReturn(false);
        given(reservationRepository.save(any())).willAnswer(invocation -> {
            LimitedReservation r = invocation.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 10L);
            return r;
        });

        // When
        LimitedReservationResponse result = reservationService.createReservationWithLock(authUser, request);

        // Then
        assertThat(result.status()).isEqualTo(WAITING);
        verify(queueService).enqueue(anyLong(), anyLong(), any());
    }

    @Test
    void 예약_생성_실패_중복예약() {
        // Given
        given(eventRepository.findById(anyLong())).willReturn(Optional.of(event));
        given(reservationRepository.findByUsersIdAndLimitedEvent(anyLong(), any()))
                .willReturn(Optional.of(mock(LimitedReservation.class)));

        // When & Then
        assertThrows(BadRequestException.class,
                () -> reservationService.createReservationWithLock(authUser, request));
    }

    @Test
    void 예약_취소_성공() {
        // Given
        LimitedReservation reservation = mock(LimitedReservation.class);

        given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
        given(reservation.getUsers()).willReturn(user);
        given(user.getId()).willReturn(authUser.getUserId());
        given(reservation.getStatus()).willReturn(RESERVED);
        given(reservation.getLimitedEvent()).willReturn(event);
        given(event.getId()).willReturn(1L);

        // When
        reservationService.cancelReservation(authUser, 1L);

        // Then
        verify(reservation).markCanceled();
        verify(queueService).remove(anyLong(), anyLong());
        verify(historyRepository, times(2)).save(any());
    }

    @Test
    void 예약_취소_실패_다른사람예약() {
        // Given
        Users otherUser = Users.builder().id(10L).build();
        LimitedReservation reservation = mock(LimitedReservation.class);

        given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
        given(reservation.getUsers()).willReturn(otherUser);

        // When & Then
        assertThrows(BadRequestException.class,
                () -> reservationService.cancelReservation(authUser, 1L));
    }

    @Test
    void 예약_만료_일괄처리() {
        // Given
        LimitedReservation expiredReservation = mock(LimitedReservation.class);

        given(reservationRepository.findAllByStatusAndExpiresAtBefore(eq(RESERVED), any())).willReturn(List.of(expiredReservation));
        given(expiredReservation.getStatus()).willReturn(RESERVED);
        given(expiredReservation.getLimitedEvent()).willReturn(event);
        given(event.getId()).willReturn(1L);

        // 승급 대상 추가
        LimitedReservation promoted = mock(LimitedReservation.class);

        given(queueService.dequeue(anyLong())).willReturn(1L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(promoted));
        given(promoted.getStatus()).willReturn(WAITING);
        given(promoted.getLimitedEvent()).willReturn(event);

        // When
        reservationService.expireReservationAndPromote();

        // Then
        verify(expiredReservation).markCanceled();
        verify(promoted).markSuccess();
        verify(historyRepository, atLeastOnce()).save(any());
    }

    @Test
    void 조건_미충족_예약_단건_만료_처리() {
        // Given
        LimitedReservation reservation = mock(LimitedReservation.class);

        given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
        given(reservation.getStatus()).willReturn(WAITING);

        // When
        reservationService.expireReservationAndPromoteOne(1L);

        // Then
        verify(reservation, never()).markCanceled();
        verify(historyRepository, never()).save(any());
    }

    @Test
    void 예약_만료_처리_성공() {
        // Given
        LimitedReservation reservation = mock(LimitedReservation.class);

        given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
        given(reservation.getStatus()).willReturn(RESERVED);
        given(reservation.isExpired()).willReturn(true);
        given(reservation.getLimitedEvent()).willReturn(event);
        given(event.getId()).willReturn(1L);

        // When
        reservationService.expireReservationAndPromoteOne(1L);

        // Then
        verify(reservation).markCanceled();
        verify(historyRepository, times(2)).save(any());
    }

    @Test
    void 주문ID로_예약_조회_성공() {
        // Given
        LimitedReservation reservation = mock(LimitedReservation.class);

        given(reservationRepository.findByOrderId(1L)).willReturn(Optional.of(reservation));

        // When
        LimitedReservation result = reservationService.findReservationByOrderIdOrElseThrow(1L);

        // Then
        assertThat(result).isEqualTo(reservation);
    }

    @Test
    void 주문ID로_예약_조회_실패() {
        // Given
        given(reservationRepository.findByOrderId(anyLong())).willReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class,
                () -> reservationService.findReservationByOrderIdOrElseThrow(1L));
    }
}