package com.example.ililbooks.domain.limitedreservation.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedreservation.dto.request.LimitedReservationCreateRequest;
import com.example.ililbooks.domain.limitedreservation.dto.response.LimitedReservationResponse;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import com.example.ililbooks.domain.limitedreservation.repository.LimitedReservationRepository;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.service.OrderService;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.LoginType;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.domain.user.repository.UserRepository;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.domain.limitedevent.repository.LimitedEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private UserRepository userRepository;

    @Mock
    private OrderService orderService;

    private AuthUser authUser;
    private Users user;
    private LimitedEvent limitedEvent;

    @BeforeEach
    void setUp() {
        authUser = new AuthUser(1L, "test@sample.com", "testUser", UserRole.ROLE_USER);

        user = Users.builder()
                .id(1L)
                .email("test@sample.com")
                .nickname("nickname")
                .password("password")
                .zipCode("12345")
                .roadAddress("street")
                .detailedAddress("details")
                .contactNumber("010-0000-0000")
                .loginType(LoginType.EMAIL)
                .userRole(UserRole.ROLE_USER)
                .isDeleted(false)
                .build();

        Book book = Book.of(user, "테스트책", "작가", BigDecimal.valueOf(15000), "소설", 10, "ISBN00000", "출판사");

        limitedEvent = LimitedEvent.of(book, "한정판 이벤트", Instant.now(), Instant.now().plusSeconds(3600), "이벤트 설명", 5);
    }

    @Test
    void 예약생성_성공() {
        // Given
        LimitedReservationCreateRequest request = new LimitedReservationCreateRequest(limitedEvent.getId());

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(limitedEventRepository.findById(limitedEvent.getId())).willReturn(Optional.of(limitedEvent));
        given(limitedReservationRepository.findByUsersAndLimitedEvent(user, limitedEvent)).willReturn(Optional.empty());
        given(limitedReservationRepository.countByLimitedEventAndStatus(limitedEvent, LimitedReservationStatus.SUCCESS)).willReturn(2L);
        given(limitedReservationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // When
        LimitedReservationResponse response = limitedReservationService.createReservation(authUser, request);

        // Then
        assertThat(response.status()).isEqualTo(LimitedReservationStatus.SUCCESS);
    }

    @Test
    void 예약생성_중복예외() {
        // Given
        LimitedReservationCreateRequest request = new LimitedReservationCreateRequest(limitedEvent.getId());
        LimitedReservation duplicated = LimitedReservation.of(user, limitedEvent, LimitedReservationStatus.SUCCESS, Instant.now());

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(limitedEventRepository.findById(limitedEvent.getId())).willReturn(Optional.of(limitedEvent));
        given(limitedReservationRepository.findByUsersAndLimitedEvent(user, limitedEvent)).willReturn(Optional.of(duplicated));

        // When & Then
        assertThrows(BadRequestException.class, () -> limitedReservationService.createReservation(authUser, request));
    }

    @Test
    void 예약조회_성공() {
        // Given
        LimitedReservation reservation = LimitedReservation.of(user, limitedEvent, LimitedReservationStatus.SUCCESS, Instant.now());

        given(limitedReservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        // When
        LimitedReservationResponse response = limitedReservationService.getReservationByUser(authUser, 1L);

        // Then
        assertThat(response.userId()).isEqualTo(1L);
    }

    @Test
    void 예약조회_권한없음예외() {
        // Given
        Users anotherUser = Users.builder().id(2L).email("a@a.com").nickname("a").loginType(LoginType.EMAIL).userRole(UserRole.ROLE_USER).isDeleted(false).build();
        LimitedReservation reservation = LimitedReservation.of(anotherUser, limitedEvent, LimitedReservationStatus.SUCCESS, Instant.now());

        given(limitedReservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        // When & Then
        assertThrows(BadRequestException.class, () -> limitedReservationService.getReservationByUser(authUser, 1L));
    }

    @Test
    void 예약취소_성공() {
        // Given
        LimitedReservation reservation = LimitedReservation.of(user, limitedEvent, LimitedReservationStatus.SUCCESS, Instant.now());

        given(limitedReservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        // When
        limitedReservationService.cancelReservation(authUser, 1L);

        // Then
        assertThat(reservation.getStatus()).isEqualTo(LimitedReservationStatus.CANCELED);
    }

    @Test
    void 예약생성_주문연동_성공() {
        LimitedReservationCreateRequest request = new LimitedReservationCreateRequest(limitedEvent.getId());
        Order dummyOrder = Order.of(user, BigDecimal.ZERO); // 가짜 주문 객체

        // ID 수동 설정
        ReflectionTestUtils.setField(dummyOrder, "id", 100L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(limitedEventRepository.findById(limitedEvent.getId())).willReturn(Optional.of(limitedEvent));
        given(limitedReservationRepository.findByUsersAndLimitedEvent(user, limitedEvent)).willReturn(Optional.empty());
        given(limitedReservationRepository.countByLimitedEventAndStatus(limitedEvent, LimitedReservationStatus.SUCCESS)).willReturn(1L);
        given(orderService.createOrderForReservation(user)).willReturn(dummyOrder);
        given(limitedReservationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // When
        LimitedReservationResponse response = limitedReservationService.createReservation(authUser, request);

        // Then
        assertThat(response.status()).isEqualTo(LimitedReservationStatus.SUCCESS);
        assertThat(response.orderId()).isNotNull();
    }
}