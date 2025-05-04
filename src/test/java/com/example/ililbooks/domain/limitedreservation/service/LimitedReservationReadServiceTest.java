package com.example.ililbooks.domain.limitedreservation.service;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedevent.repository.LimitedEventRepository;
import com.example.ililbooks.domain.limitedreservation.dto.request.LimitedReservationStatusFilterRequest;
import com.example.ililbooks.domain.limitedreservation.dto.response.LimitedReservationResponse;
import com.example.ililbooks.domain.limitedreservation.dto.response.LimitedReservationStatusResponse;
import com.example.ililbooks.domain.limitedreservation.dto.response.LimitedReservationSummaryResponse;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.repository.LimitedReservationRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class LimitedReservationReadServiceTest {

    @Mock
    private LimitedReservationRepository reservationRepository;

    @Mock
    private LimitedEventRepository eventRepository;

    @InjectMocks
    private LimitedReservationReadService readService;

    private AuthUser authUser;
    private LimitedReservation reservation;
    private LimitedEvent limitedEvent;

    @BeforeEach
    void setUp() {
        authUser = AuthUser.builder()
                .userId(1L)
                .email("test@test.com")
                .nickname("테스트 유저")
                .role(UserRole.ROLE_USER)
                .build();

        reservation = mock(LimitedReservation.class);
        limitedEvent = mock(LimitedEvent.class);
    }

    @Test
    void 내_예약_조회_성공() {
        // Given
        LimitedEvent limitedEvent = mock(LimitedEvent.class);

        given(limitedEvent.getId()).willReturn(1L);
        given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
        given(reservation.getUsers()).willReturn(AuthUserToUsers(authUser));
        given(reservation.getLimitedEvent()).willReturn(limitedEvent);

        // When
        LimitedReservationResponse response = readService.getReservationByUser(authUser, 1L);

        // Then
        assertThat(response).isNotNull();
    }

    @Test
    void 다른_유저_예약_조회_실패() {
        // Given
        given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
        given(reservation.getUsers()).willReturn(UsersWithDifferentId());

        // When & Then
        assertThrows(BadRequestException.class,
                () -> readService.getReservationByUser(authUser, 1L));
    }

    @Test
    void 예약상태_조회_성공() {
        // Given
        LimitedEvent limitedEvent = mock(LimitedEvent.class);

        given(limitedEvent.getTitle()).willReturn("테스트 이벤트");
        given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
        given(reservation.getUsers()).willReturn(AuthUserToUsers(authUser));
        given(reservation.getLimitedEvent()).willReturn(limitedEvent);

        // When
        LimitedReservationStatusResponse response = readService.getReservationStatus(authUser, 1L);

        // Then
        assertThat(response).isNotNull();
    }

    @Test
    void 예약상태_조회_실패_다른사람예약() {
        // Given
        given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
        given(reservation.getUsers()).willReturn(UsersWithDifferentId());

        // When & Then
        assertThrows(BadRequestException.class,
                () -> readService.getReservationStatus(authUser, 1L));
    }

    @Test
    void 행사별_예약_전체조회_성공() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        LimitedEvent limitedEvent = mock(LimitedEvent.class);
        Users user = mock(Users.class);

        given(limitedEvent.getId()).willReturn(1L);
        given(reservation.getLimitedEvent()).willReturn(limitedEvent);
        given(user.getId()).willReturn(1L);
        given(reservation.getUsers()).willReturn(user);
        given(eventRepository.findById(anyLong())).willReturn(Optional.of(limitedEvent));
        given(reservationRepository.findAllByLimitedEvent(any(), eq(pageable))).willReturn(new PageImpl<>(List.of(reservation)));

        // When
        var result = readService.getReservationsByEvent(1L, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void 행사별_상태별_예약_리스트_조회_성공() {
        // Given
        LimitedEvent limitedEvent = mock(LimitedEvent.class);
        Users user = mock(Users.class);

        given(limitedEvent.getId()).willReturn(1L);
        given(reservation.getLimitedEvent()).willReturn(limitedEvent);
        given(user.getId()).willReturn(1L);
        given(reservation.getUsers()).willReturn(user);
        given(eventRepository.findById(anyLong())).willReturn(Optional.of(limitedEvent));
        given(reservationRepository.findAllByLimitedEventAndStatusIn(any(), anyList())).willReturn(List.of(reservation));

        // When
        var result = readService.getReservationsByEventAndStatus(1L, List.of(RESERVED, WAITING));

        // Then
        assertThat(result).isNotEmpty();
    }

    @Test
    void 예약_통계조회_성공() {
        // Given
        given(eventRepository.findById(anyLong())).willReturn(Optional.of(limitedEvent));
        given(reservationRepository.countByLimitedEventAndStatus(limitedEvent, RESERVED)).willReturn(1L);
        given(reservationRepository.countByLimitedEventAndStatus(limitedEvent, WAITING)).willReturn(2L);
        given(reservationRepository.countByLimitedEventAndStatus(limitedEvent, CANCELED)).willReturn(3L);

        // When
        LimitedReservationSummaryResponse summary = readService.getReservationSummary(1L);

        // Then
        assertThat(summary.successCount()).isEqualTo(1);
        assertThat(summary.waitingCount()).isEqualTo(2);
        assertThat(summary.canceledCount()).isEqualTo(3);
    }

    @Test
    void 필터기반_예약조회_성공() {
        // Given
        LimitedReservationStatusFilterRequest request = LimitedReservationStatusFilterRequest.builder()
                .eventId(1L)
                .statuses(List.of(RESERVED, WAITING))
                .build();

        LimitedEvent limitedEvent = mock(LimitedEvent.class);
        Users user = mock(Users.class);

        given(limitedEvent.getId()).willReturn(1L);
        given(reservation.getLimitedEvent()).willReturn(limitedEvent);
        given(user.getId()).willReturn(1L);
        given(reservation.getUsers()).willReturn(user);
        given(reservationRepository.findByFilter(anyLong(), anyList(), any(), any(), any())).willReturn(List.of(reservation));

        // When
        var result = readService.getReservationsByFilter(request);

        // Then
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("필터 기반 예약 조회 실패 - 잘못된 요청")
    void 필터기반_예약조회_실패_요청오류() {
        // Given
        LimitedReservationStatusFilterRequest request = LimitedReservationStatusFilterRequest.builder()
                .eventId(null)
                .statuses(null)
                .build();

        // When & Then
        assertThrows(BadRequestException.class,
                () -> readService.getReservationsByFilter(request));
    }

    // ---- 내부 헬퍼 메서드 ----

    private Users AuthUserToUsers(AuthUser authUser) {
        return Users.builder()
                .id(authUser.getUserId())
                .nickname(authUser.getNickname())
                .build();
    }

    private Users UsersWithDifferentId() {
        return Users.builder()
                .id(2L)
                .nickname("유저2")
                .build();
    }
}