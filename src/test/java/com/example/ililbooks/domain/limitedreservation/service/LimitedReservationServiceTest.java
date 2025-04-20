//package com.example.ililbooks.domain.limitedreservation.service;
//
//import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
//import com.example.ililbooks.domain.limitedevent.repository.LimitedEventRepository;
//import com.example.ililbooks.domain.limitedreservation.dto.request.LimitedReservationCreateRequest;
//import com.example.ililbooks.domain.limitedreservation.dto.response.LimitedReservationResponse;
//import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
//import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
//import com.example.ililbooks.domain.limitedreservation.repository.LimitedReservationRepository;
//import com.example.ililbooks.domain.user.entity.Users;
//import com.example.ililbooks.domain.user.enums.LoginType;
//import com.example.ililbooks.domain.user.enums.UserRole;
//import com.example.ililbooks.domain.user.repository.UserRepository;
//import com.example.ililbooks.global.dto.AuthUser;
//import com.example.ililbooks.global.exception.BadRequestException;
//import com.example.ililbooks.global.exception.ErrorMessage;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.given;
//
//@ExtendWith(MockitoExtension.class)
//class LimitedReservationServiceTest {
//
//    @Mock
//    private LimitedReservationRepository limitedReservationRepository;
//
//    @Mock
//    private LimitedEventRepository limitedEventRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @InjectMocks
//    private LimitedReservationService limitedReservationService;
//
//    private static final Long TEST_USER_ID = 1L;
//    private static final Long TEST_EVENT_ID = 10L;
//    private static final Long TEST_RESERVATION_ID = 100L;
//
//    private static final AuthUser TEST_AUTH_USER = new AuthUser(TEST_USER_ID, "user@sample.com", "tester", UserRole.ROLE_USER);
//
//    private static final Users TEST_USER = Users.builder()
//            .id(TEST_USER_ID)
//            .email("user@sample.com")
//            .nickname("테스트닉넴")
//            .password("encoded")
//            .zipCode("12345")
//            .roadAddress("서울시 성북구")
//            .detailedAddress("302호")
//            .contactNumber("010-1234-5678")
//            .loginType(LoginType.EMAIL)
//            .userRole(UserRole.ROLE_USER)
//            .deletedAt(null)
//            .build();
//
//    private static final LimitedEvent TEST_EVENT = createTestEvent();
//
//    @Test
//    void 예약_가능시_SUCCESS_상태로_생성() {
//        // Given
//        LimitedReservationCreateRequest request = new LimitedReservationCreateRequest(TEST_EVENT_ID);
//
//        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(TEST_USER));
//        given(limitedEventRepository.findById(TEST_EVENT_ID)).willReturn(Optional.of(TEST_EVENT));
//        given(limitedReservationRepository.findByUsersAndLimitedEvent(TEST_USER, TEST_EVENT)).willReturn(Optional.empty());
//        given(limitedReservationRepository.countByLimitedEventAndStatus(TEST_EVENT, LimitedReservationStatus.SUCCESS)).willReturn(1L);
//        given(limitedReservationRepository.save(any())).willAnswer(invocation -> {
//            LimitedReservation saved = invocation.getArgument(0);
//            ReflectionTestUtils.setField(saved, "id", TEST_RESERVATION_ID);
//            return saved;
//        });
//
//        // When
//        LimitedReservationResponse response = limitedReservationService.createReservation(TEST_AUTH_USER, request);
//
//        // Then
//        assertThat(response.getStatus()).isEqualTo(LimitedReservationStatus.SUCCESS);
//        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
//    }
//
//    @Test
//    void 예약_불가시_WAITING_상태로_생성() {
//        // Given
//        LimitedReservationCreateRequest request = new LimitedReservationCreateRequest(TEST_EVENT_ID);
//
//        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(TEST_USER));
//        given(limitedEventRepository.findById(TEST_EVENT_ID)).willReturn(Optional.of(TEST_EVENT));
//        given(limitedReservationRepository.findByUsersAndLimitedEvent(TEST_USER, TEST_EVENT)).willReturn(Optional.empty());
//        given(limitedReservationRepository.countByLimitedEventAndStatus(TEST_EVENT, LimitedReservationStatus.SUCCESS)).willReturn(100L); // full
//        given(limitedReservationRepository.save(any())).willAnswer(invocation -> {
//            LimitedReservation saved = invocation.getArgument(0);
//            ReflectionTestUtils.setField(saved, "id", TEST_RESERVATION_ID);
//            return saved;
//        });
//
//        // When
//        LimitedReservationResponse response = limitedReservationService.createReservation(TEST_AUTH_USER, request);
//
//        // Then
//        assertThat(response.getStatus()).isEqualTo(LimitedReservationStatus.WAITING);
//    }
//
//    @Test
//    void 이미_예약한_행사일경우_예외처리() {
//        // Given
//        LimitedReservationCreateRequest request = new LimitedReservationCreateRequest(TEST_EVENT_ID);
//        LimitedReservation existing = createTestReservation(500L, TEST_USER, TEST_EVENT, LimitedReservationStatus.SUCCESS);
//
//        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(TEST_USER));
//        given(limitedEventRepository.findById(TEST_EVENT_ID)).willReturn(Optional.of(TEST_EVENT));
//        given(limitedReservationRepository.findByUsersAndLimitedEvent(TEST_USER, TEST_EVENT)).willReturn(Optional.of(existing));
//
//        // When & Then
//        assertThatThrownBy(() -> limitedReservationService.createReservation(TEST_AUTH_USER, request))
//                .isInstanceOf(BadRequestException.class)
//                .hasMessage(ErrorMessage.ALREADY_RESERVED_EVENT.getMessage());
//    }
//
//    @Test
//    void 단건_예약_조회() {
//        // Given
//        LimitedReservation reservation = createTestReservation(TEST_RESERVATION_ID, TEST_USER, TEST_EVENT, LimitedReservationStatus.SUCCESS);
//        given(limitedReservationRepository.findById(TEST_RESERVATION_ID)).willReturn(Optional.of(reservation));
//
//        // When
//        LimitedReservationResponse response = limitedReservationService.getReservationByUser(TEST_AUTH_USER, TEST_RESERVATION_ID);
//
//        // Then
//        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
//    }
//
//    @Test
//    void 본인_예약이_아닐경우_예외발생() {
//        // Given
//        Users otherUser = Users.builder().id(2L).email("other@sample.com").nickname("다른유저").userRole(UserRole.ROLE_USER).loginType(LoginType.EMAIL).build();
//        LimitedReservation reservation = createTestReservation(TEST_RESERVATION_ID, otherUser, TEST_EVENT, LimitedReservationStatus.SUCCESS);
//
//        given(limitedReservationRepository.findById(TEST_RESERVATION_ID)).willReturn(Optional.of(reservation));
//
//        // When & Then
//        assertThatThrownBy(() -> limitedReservationService.getReservationByUser(TEST_AUTH_USER, TEST_RESERVATION_ID))
//                .isInstanceOf(BadRequestException.class)
//                .hasMessage(ErrorMessage.NOT_OWN_RESERVATION.getMessage());
//    }
//
//    @Test
//    void 본인_예약_취소_성공() {
//        // Given
//        LimitedReservation reservation = createTestReservation(TEST_RESERVATION_ID, TEST_USER, TEST_EVENT, LimitedReservationStatus.SUCCESS);
//        given(limitedReservationRepository.findById(TEST_RESERVATION_ID)).willReturn(Optional.of(reservation));
//
//        // When
//        limitedReservationService.cancelReservation(TEST_AUTH_USER, TEST_RESERVATION_ID);
//
//        // Then
//        assertThat(reservation.getStatus()).isEqualTo(LimitedReservationStatus.CANCELED);
//    }
//
//    // 헬퍼 메서드 - 테스트용 행사
//    private static LimitedEvent createTestEvent() {
//        LimitedEvent limitedEvent = LimitedEvent.builder()
//                .book(null)
//                .title("행사제목")
//                .startTime(LocalDateTime.now().minusDays(1))
//                .endTime(LocalDateTime.now().plusDays(1))
//                .contents("설명")
//                .bookQuantity(100)
//                .build();
//        ReflectionTestUtils.setField(limitedEvent, "id", TEST_EVENT_ID);
//        return limitedEvent;
//    }
//
//    // 헬퍼 메서드 = 테스트용 예약
//    private static LimitedReservation createTestReservation(Long id, Users user, LimitedEvent event, LimitedReservationStatus status) {
//        LimitedReservation reservation = LimitedReservation.createFrom(user, event, status, LocalDateTime.now().plusHours(24));
//        ReflectionTestUtils.setField(reservation, "id", id);
//        return reservation;
//    }
//}