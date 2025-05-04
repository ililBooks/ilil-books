package com.example.ililbooks.domain.payment.service;

import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import com.example.ililbooks.domain.limitedreservation.service.LimitedReservationService;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.enums.DeliveryStatus;
import com.example.ililbooks.domain.order.enums.LimitedType;
import com.example.ililbooks.domain.order.enums.OrderStatus;
import com.example.ililbooks.domain.order.enums.PaymentStatus;
import com.example.ililbooks.domain.order.service.OrderService;
import com.example.ililbooks.domain.payment.dto.request.PaymentRequest;
import com.example.ililbooks.domain.payment.dto.request.PaymentVerificationRequest;
import com.example.ililbooks.domain.payment.dto.response.PaymentResponse;
import com.example.ililbooks.domain.payment.entity.Payment;
import com.example.ililbooks.domain.payment.enums.PGProvider;
import com.example.ililbooks.domain.payment.enums.PayStatus;
import com.example.ililbooks.domain.payment.enums.PaymentMethod;
import com.example.ililbooks.domain.payment.repository.PaymentRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.ForbiddenException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.request.PrepareData;
import com.siot.IamportRestClient.response.IamportResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import static com.example.ililbooks.global.exception.ErrorMessage.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderService orderService;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private IamportClient iamportClient;
    @Mock
    private LimitedReservationService limitedReservationService;

    @InjectMocks
    private PaymentService paymentService;

    private AuthUser authUser, anotherUser;
    private Order order, limitedOrder;
    private Payment paidPayment, pendingPayment, failedPayment;
    private PaymentVerificationRequest paymentVerificationRequest;

    @BeforeEach
    void setUp() {
        authUser = AuthUser.builder()
                .userId(1L)
                .email("email@email.com")
                .nickname("nickname")
                .role(UserRole.ROLE_USER)
                .build();

        anotherUser = AuthUser.builder()
                .userId(100L)
                .email("email@email.com")
                .nickname("nickname")
                .role(UserRole.ROLE_USER)
                .build();

        order = Order.builder()
                .id(1L)
                .users(Users.fromAuthUser(authUser))
                .number("order-number")
                .limitedType(LimitedType.REGULAR)
                .build();

        limitedOrder = Order.builder()
                .id(1L)
                .users(Users.fromAuthUser(authUser))
                .number("limited-order-number")
                .limitedType(LimitedType.LIMITED)
                .build();

        paidPayment = Payment.builder()
                .id(1L)
                .pg(PGProvider.KG)
                .paymentMethod(PaymentMethod.CARD)
                .payStatus(PayStatus.PAID)
                .order(order)
                .build();

        pendingPayment = Payment.builder()
                .id(1L)
                .payStatus(PayStatus.READY)
                .pg(PGProvider.KG)
                .paymentMethod(PaymentMethod.CARD)
                .order(order)
                .build();

        failedPayment = Payment.builder()
                .id(1L)
                .payStatus(PayStatus.FAILED)
                .pg(PGProvider.KG)
                .paymentMethod(PaymentMethod.CARD)
                .order(order)
                .build();

        paymentVerificationRequest = PaymentVerificationRequest.builder()
                .impUid("imp_123")
                .merchantUid("merchantUid_123")
                .amount(new BigDecimal(100))
                .build();
    }

    /* prepareOrder */
    /* --- 실패 케이스 --- */
    @Test
    void 결제_준비_본인_주문이_아니라_실패() {
        // given
        Long orderId = 1L;

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(order);

        // when & then
        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
                () -> paymentService.prepareOrder(anotherUser, orderId));
        assertEquals(forbiddenException.getMessage(), NOT_OWN_ORDER.getMessage());
    }

    @Test
    void 결제_준비_주문_상태가_대기가_아니라_실패() {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.ORDERED);    // false
        ReflectionTestUtils.setField(order, "paymentStatus", PaymentStatus.PENDING);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.READY);

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(order);

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> paymentService.prepareOrder(authUser, orderId));
        assertEquals(badRequestException.getMessage(), CANNOT_CREATE_PAYMENT.getMessage());
    }

    @Test
    void 결제_준비_결제_상태가_대기_또는_실패가_아니라_실패() {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.PENDING);
        ReflectionTestUtils.setField(order, "paymentStatus", PaymentStatus.PAID);   // false
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.READY);

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(order);

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> paymentService.prepareOrder(authUser, orderId));
        assertEquals(badRequestException.getMessage(), CANNOT_CREATE_PAYMENT.getMessage());
    }

    @Test
    void 결제_준비_배달_상태가_준비가_아니라_실패() {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.PENDING);
        ReflectionTestUtils.setField(order, "paymentStatus", PaymentStatus.PENDING);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.IN_TRANSIT);    // false

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(order);

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> paymentService.prepareOrder(authUser, orderId));
        assertEquals(badRequestException.getMessage(), CANNOT_CREATE_PAYMENT.getMessage());
    }

    @Test
    void 결제_준비_최근_결제_내역이_있을_때_실패_상태가_아니면_실패() {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.PENDING);
        ReflectionTestUtils.setField(order, "paymentStatus", PaymentStatus.FAILED);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.READY);

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(order);
        given(paymentRepository.findTopByOrderIdOrderByCreatedAtDesc(anyLong())).willReturn(Optional.of(paidPayment));

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> paymentService.prepareOrder(authUser, orderId));
        assertEquals(badRequestException.getMessage(), CANNOT_CREATE_PAYMENT.getMessage());
    }

    /* --- 성공 케이스 --- */
    @Test
    void 결제_준비_최근_결제_내역_없이_성공() throws IamportResponseException, IOException {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.PENDING);
        ReflectionTestUtils.setField(order, "paymentStatus", PaymentStatus.PENDING);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.READY);

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(order);
        given(paymentRepository.findTopByOrderIdOrderByCreatedAtDesc(anyLong())).willReturn(Optional.empty());

        // when
        PaymentResponse result = paymentService.prepareOrder(authUser, orderId);

        // then
        assertNotNull(result);
        assertNotNull(result.merchantUid());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(iamportClient, times(1)).postPrepare(any(PrepareData.class));
    }

    @Test
    void 결제_준비_최근_결제_내역이_실패라_성공() throws IamportResponseException, IOException {
        // given
        Long orderId = 1L;
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.PENDING);
        ReflectionTestUtils.setField(order, "paymentStatus", PaymentStatus.PENDING);
        ReflectionTestUtils.setField(order, "deliveryStatus", DeliveryStatus.READY);

        given(orderService.findByIdOrElseThrow(anyLong())).willReturn(order);
        given(paymentRepository.findTopByOrderIdOrderByCreatedAtDesc(anyLong())).willReturn(Optional.of(failedPayment));

        // when
        PaymentResponse result = paymentService.prepareOrder(authUser, orderId);

        // then
        assertNotNull(result);
        assertNotNull(result.merchantUid());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(iamportClient, times(1)).postPrepare(any(PrepareData.class));
    }

    /* findPaymentRequestData */
    @Test
    void 결제_요청_전달_결제가_없어_실패() {
        // given
        Long paymentId = 1L;

        given(paymentRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> paymentService.findPaymentRequestData(paymentId));
        assertEquals(notFoundException.getMessage(), NOT_FOUND_PAYMENT.getMessage());
    }

    @Test
    void 결제_요청_전달_결제_상태가_준비_상태가_아니라_실패() {
        // given
        Long paymentId = 1L;

        given(paymentRepository.findById(anyLong())).willReturn(Optional.of(paidPayment));

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> paymentService.findPaymentRequestData(paymentId));
        assertEquals(badRequestException.getMessage(), CANNOT_REQUEST_PAYMENT.getMessage());
    }

    @Test
    void 결제_요청_전달_성공() {
        // given
        Long paymentId = 1L;

        given(paymentRepository.findById(anyLong())).willReturn(Optional.of(pendingPayment));

        // when
        PaymentRequest result = paymentService.findPaymentRequestData(paymentId);

        // then
        assertNotNull(result);
    }

    /* verifyPayment */
    /* --- 실패 케이스 --- */
    @Test
    void 결제_요청_검증_결제를_찾을_수_없어_실패() throws IamportResponseException, IOException {
        // given
        given(iamportClient.paymentByImpUid(anyString())).willReturn(mock(IamportResponse.class));
        given(paymentRepository.findByMerchantUid(anyString())).willReturn(Optional.empty());

        // when & then
        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> paymentService.verifyPayment(authUser, paymentVerificationRequest));
        assertEquals(notFoundException.getMessage(), NOT_FOUND_PAYMENT.getMessage());
    }

    @Test
    void 결제_요청_검증_본인_주문이_아니라_실패() throws IamportResponseException, IOException {
        // given
        given(iamportClient.paymentByImpUid(anyString())).willReturn(mock(IamportResponse.class));
        given(paymentRepository.findByMerchantUid(anyString())).willReturn(Optional.of(pendingPayment));

        // when & then
        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
                () -> paymentService.verifyPayment(anotherUser, paymentVerificationRequest));
        assertEquals(forbiddenException.getMessage(), NOT_OWN_PAYMENT.getMessage());
    }

    @Test
    void 결제_요청_검증_한정판_예약_결제_대기_상태가_아니라_실패() throws IamportResponseException, IOException {
        // given
        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse = new IamportResponse<>();
        com.siot.IamportRestClient.response.Payment iamportResponsePayment = new com.siot.IamportRestClient.response.Payment();

        ReflectionTestUtils.setField(iamportResponsePayment, "amount", new BigDecimal(100));
        ReflectionTestUtils.setField(iamportResponse, "response", iamportResponsePayment);

        LimitedReservation limitedReservation = LimitedReservation.builder()
                .order(limitedOrder)
                .status(LimitedReservationStatus.CANCELED)
                .build();
        ReflectionTestUtils.setField(pendingPayment, "order", limitedOrder);

        given(iamportClient.paymentByImpUid(anyString())).willReturn(iamportResponse);
        given(paymentRepository.findByMerchantUid(anyString())).willReturn(Optional.of(pendingPayment));
        given(limitedReservationService.findReservationByOrderIdOrElseThrow(anyLong())).willReturn(limitedReservation);

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> paymentService.verifyPayment(authUser, paymentVerificationRequest));
        assertEquals(badRequestException.getMessage(), INVALID_RESERVATION_STATUS_FOR_PAYMENT.getMessage());
    }

    /* --- 성공 케이스 --- */
    @Test
    void 결제_요청_검증_동일한_금액이라_주문_승인_성공() throws IamportResponseException, IOException {
        // given
        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse = new IamportResponse<>();
        com.siot.IamportRestClient.response.Payment iamportResponsePayment = new com.siot.IamportRestClient.response.Payment();

        ReflectionTestUtils.setField(iamportResponsePayment, "amount", new BigDecimal(100));
        ReflectionTestUtils.setField(iamportResponse, "response", iamportResponsePayment);

        given(iamportClient.paymentByImpUid(anyString())).willReturn(iamportResponse);
        given(paymentRepository.findByMerchantUid(anyString())).willReturn(Optional.of(pendingPayment));

        // when
        PaymentResponse result = paymentService.verifyPayment(authUser, paymentVerificationRequest);

        // then
        assertNotNull(result);
        assertEquals(PayStatus.PAID.name(), result.payStatus());
    }

    @Test
    void 결제_요청_검증_금액이_동일하지_않아_결제_거부_성공() throws IamportResponseException, IOException {
        // given
        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse = new IamportResponse<>();
        com.siot.IamportRestClient.response.Payment iamportResponsePayment = new com.siot.IamportRestClient.response.Payment();

        ReflectionTestUtils.setField(iamportResponsePayment, "amount", new BigDecimal(10000));
        ReflectionTestUtils.setField(iamportResponse, "response", iamportResponsePayment);

        given(iamportClient.paymentByImpUid(anyString())).willReturn(iamportResponse);
        given(paymentRepository.findByMerchantUid(anyString())).willReturn(Optional.of(pendingPayment));

        // when
        PaymentResponse result = paymentService.verifyPayment(authUser, paymentVerificationRequest);

        // then
        assertNotNull(result);
        assertEquals(PayStatus.FAILED.name(), result.payStatus());
    }

    @Test
    void 결제_요청_검증_한정판_예약_결제_성공() throws IamportResponseException, IOException {
        // given
        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse = new IamportResponse<>();
        com.siot.IamportRestClient.response.Payment iamportResponsePayment = new com.siot.IamportRestClient.response.Payment();

        ReflectionTestUtils.setField(iamportResponsePayment, "amount", new BigDecimal(100));
        ReflectionTestUtils.setField(iamportResponse, "response", iamportResponsePayment);

        LimitedReservation limitedReservation = LimitedReservation.builder()
                .order(limitedOrder)
                .status(LimitedReservationStatus.RESERVED)
                .build();
        ReflectionTestUtils.setField(pendingPayment, "order", limitedOrder);

        given(iamportClient.paymentByImpUid(anyString())).willReturn(iamportResponse);
        given(paymentRepository.findByMerchantUid(anyString())).willReturn(Optional.of(pendingPayment));
        given(limitedReservationService.findReservationByOrderIdOrElseThrow(anyLong())).willReturn(limitedReservation);

        // when
        PaymentResponse result = paymentService.verifyPayment(authUser, paymentVerificationRequest);

        // then
        assertNotNull(result);
        assertEquals(PayStatus.PAID.name(), result.payStatus());
    }

    /* findPaymentById */
    @Test
    void 결제_조회_결제를_찾을_수_없어_실패() {
        // given
        Long paymentId = 1L;

        given(paymentRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> paymentService.findPaymentById(authUser, paymentId));
        assertEquals(notFoundException.getMessage(), NOT_FOUND_PAYMENT.getMessage());
    }

    @Test
    void 결제_조회_본인_주문이_아니라_실패() {
        // given
        Long paymentId = 1L;

        given(paymentRepository.findById(anyLong())).willReturn(Optional.of(pendingPayment));

        // when & then
        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
                () -> paymentService.findPaymentById(anotherUser, paymentId));
        assertEquals(forbiddenException.getMessage(), NOT_OWN_PAYMENT.getMessage());
    }

    @Test
    void 결제_조회_성공() {
        // given
        Long paymentId = 1L;

        given(paymentRepository.findById(anyLong())).willReturn(Optional.of(pendingPayment));

        // when
        PaymentResponse result = paymentService.findPaymentById(authUser, paymentId);

        // then
        assertNotNull(result);
        assertEquals(result.payStatus(), pendingPayment.getPayStatus().name());
        assertEquals(result.merchantUid(), pendingPayment.getMerchantUid());
        assertEquals(result.impUid(), pendingPayment.getImpUid());
    }

    /* cancelPayment */
    @Test
    void 결제_취소_결제를_찾을_수_없어_실패() {
        // given
        Long paymentId = 1L;

        given(paymentRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> paymentService.cancelPayment(authUser, paymentId));
        assertEquals(notFoundException.getMessage(), NOT_FOUND_PAYMENT.getMessage());
    }

    @Test
    void 결제_취소_본인_주문이_아니라_실패() {
        // given
        Long paymentId = 1L;

        given(paymentRepository.findById(anyLong())).willReturn(Optional.of(pendingPayment));

        // when & then
        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
                () -> paymentService.cancelPayment(anotherUser, paymentId));
        assertEquals(forbiddenException.getMessage(), NOT_OWN_PAYMENT.getMessage());
    }

    @Test
    void 결제_취소_결제_완료_상태가_아니라_실패() {
        // given
        Long paymentId = 1L;

        given(paymentRepository.findById(anyLong())).willReturn(Optional.of(pendingPayment));

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> paymentService.cancelPayment(authUser, paymentId));
        assertEquals(badRequestException.getMessage(), CANNOT_CANCEL_PAYMENT.getMessage());
    }

    @Test
    void 결제_취소_포트_응답_실패하여_실패() throws IamportResponseException, IOException {
        // given
        Long paymentId = 1L;
        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse = new IamportResponse<>();
        ReflectionTestUtils.setField(iamportResponse, "response", null);

        given(paymentRepository.findById(anyLong())).willReturn(Optional.of(paidPayment));
        given(iamportClient.cancelPaymentByImpUid(any(CancelData.class))).willReturn(iamportResponse);

        // when & then
        RuntimeException runtimeException = assertThrows(RuntimeException.class,
                () -> paymentService.cancelPayment(authUser, paymentId));
        assertEquals(runtimeException.getMessage(), CANCEL_PAYMENT_FAILED.getMessage());
    }

    @Test
    void 결제_취소_성공() throws IamportResponseException, IOException {
        // given
        Long paymentId = 1L;
        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse = new IamportResponse<>();
        com.siot.IamportRestClient.response.Payment iamportResponsePayment = new com.siot.IamportRestClient.response.Payment();

        ReflectionTestUtils.setField(iamportResponsePayment, "amount", new BigDecimal(10000));
        ReflectionTestUtils.setField(iamportResponse, "response", iamportResponsePayment);

        given(paymentRepository.findById(anyLong())).willReturn(Optional.of(paidPayment));
        given(iamportClient.cancelPaymentByImpUid(any(CancelData.class))).willReturn(iamportResponse);

        // when
        paymentService.cancelPayment(authUser, paymentId);

        // then
        assertEquals(PayStatus.CANCELLED, paidPayment.getPayStatus());
        assertEquals(PaymentStatus.CANCELLED, order.getPaymentStatus());
        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
    }

}