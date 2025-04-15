package com.example.ililbooks.domain.limitedreservation.service;

import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedevent.repository.LimitedEventRepository;
import com.example.ililbooks.domain.limitedreservation.dto.request.LimitedReservationCreateRequest;
import com.example.ililbooks.domain.limitedreservation.dto.response.LimitedReservationResponse;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.repository.LimitedReservationRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.LoginType;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.redis.RedisClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus.SUCCESS;
import static com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus.WAITING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class LimitedReservationServiceTest {

    @InjectMocks
    private LimitedReservationService limitedReservationService;

    @Mock
    private LimitedReservationRepository limitedReservationRepository;

    @Mock
    private LimitedEventRepository limitedEventRepository;

    @Mock
    private UserService userService;

    @Mock
    private RedisClient redisClient;

    @Test
    @DisplayName("예약 성공 - 재고 있음 (SUCCESS)")
    void 예약_성공_SUCCESS() {
        // Given
        Long userId = 1L;
        Long limitedEventId = 10L;
        LimitedEvent LimitedEvent = createTestEventWithId(limitedEventId, 5);
        Users user = createTestUserWithId(userId);
        AuthUser authUser = createAuthUser(userId);
        LimitedReservationCreateRequest request = new LimitedReservationCreateRequest(limitedEventId);

        given(limitedEventRepository.findById(limitedEventId)).willReturn(Optional.of(LimitedEvent));
        given(limitedReservationRepository.findByUsersIdAndLimitedEvent(userId, LimitedEvent)).willReturn(Optional.empty());
        given(limitedReservationRepository.countByLimitedEventAndStatus(LimitedEvent, SUCCESS)).willReturn(1L);
        given(userService.findByIdOrElseThrow(userId)).willReturn(user);

        // When
        LimitedReservationResponse response = limitedReservationService.createReservation(authUser, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(SUCCESS);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.limitedEventId()).isEqualTo(limitedEventId);
    }

    @Test
    void 예약_성공_WAITING() {
        // Given
        Long userId = 2L;
        Long limitedEventId = 20L;
        Users user = createTestUserWithId(userId);
        LimitedEvent limitedEvent = createTestEventWithId(limitedEventId, 3);
        AuthUser authUser = createAuthUser(userId);
        LimitedReservationCreateRequest request = new LimitedReservationCreateRequest(limitedEventId);

        given(limitedEventRepository.findById(limitedEventId)).willReturn(Optional.of(limitedEvent));
        given(limitedReservationRepository.findByUsersIdAndLimitedEvent(userId, limitedEvent)).willReturn(Optional.empty());
        given(limitedReservationRepository.countByLimitedEventAndStatus(limitedEvent, SUCCESS)).willReturn(3L);
        given(userService.findByIdOrElseThrow(userId)).willReturn(user);

        // When
        LimitedReservationResponse response = limitedReservationService.createReservation(authUser, request);

        // Then
        assertThat(response.status()).isEqualTo(WAITING);
    }

    @Test
    void 중복_예약_예외처리() {
        // Given
        Long userId = 3L;
        Long eventId = 30L;
        Users user = createTestUserWithId(userId);
        LimitedEvent limitedEvent = createTestEventWithId(eventId, 10);
        AuthUser authUser = createAuthUser(userId);
        LimitedReservationCreateRequest request = new LimitedReservationCreateRequest(eventId);

        given(limitedEventRepository.findById(eventId)).willReturn(Optional.of(limitedEvent));
        given(limitedReservationRepository.findByUsersIdAndLimitedEvent(userId, limitedEvent)).willReturn(Optional.of(mock(LimitedReservation.class)));

        // When & Then
        assertThatThrownBy(() -> limitedReservationService.createReservation(authUser, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("이미 예약된 행사입니다.");
    }

    @Test
    void 예약_조회_권한없음() {
        // Given
        Long userId = 4L;
        Long reservationId = 99L;
        AuthUser authUser = createAuthUser(userId);
        LimitedReservation reservation = mock(LimitedReservation.class);
        given(reservation.getUsers()).willReturn(createTestUserWithId(999L));
        given(limitedReservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));

        // When & Then
        assertThatThrownBy(() -> limitedReservationService.getReservationByUser(authUser, reservationId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("본인의 예약이 아닙니다");
    }

    private AuthUser createAuthUser(Long id) {
        return AuthUser.builder()
                .userId(id)
                .email("user" + id + "@example.com")
                .nickname("유저" + id)
                .role(UserRole.ROLE_USER)
                .build();
    }

    @Test
    void 예약_취소_성공() {
        // Given
        Long userId = 5L;
        Long reservationId = 88L;
        AuthUser authUser = createAuthUser(userId);
        LimitedReservation reservation = mock(LimitedReservation.class);
        given(reservation.getUsers()).willReturn(createTestUserWithId(userId));
        given(limitedReservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));

        // When
        limitedReservationService.cancelReservation(authUser, reservationId);

        // Then
        then(reservation).should().markCanceled();
    }

    private LimitedEvent createTestEventWithId(Long id, int quantity) {
        LimitedEvent event = LimitedEvent.of(
                null,
                "행사 제목",
                Instant.now().plusSeconds(60),
                Instant.now().plusSeconds(3600),
                "행사 설명",
                quantity
        );
        ReflectionTestUtils.setField(event, "id", id);
        return event;
    }

    private Users createTestUserWithId(Long id) {
        Users user = Users.of("user" + id + "@example.com", "User" + id, "010-0000-0000", LoginType.EMAIL);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
