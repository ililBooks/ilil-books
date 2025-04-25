package com.example.ililbooks.domain.payment.service;

import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.enums.DeliveryStatus;
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
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.ForbiddenException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.request.PrepareData;
import com.siot.IamportRestClient.response.IamportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderService orderService;
    private final PaymentRepository paymentRepository;
    private final IamportClient iamportClient;

    /* 결제 준비 및 결제 정보 저장 */
    @Transactional
    public PaymentResponse prepareOrder(AuthUser authUser, Long orderId) throws IamportResponseException, IOException {
        Order order = orderService.findByIdOrElseThrow(orderId);

        if (!authUser.getUserId().equals(order.getUsers().getId())) {
            throw new ForbiddenException(NOT_OWN_ORDER.getMessage());
        }

        if (!canCreatePayment(order)) {
            throw new BadRequestException(CANNOT_CREATE_PAYMENT.getMessage());
        }

        Optional<Payment> latestPaymentOpt = getTopByOrderIdOrderByCreatedAtDesc(orderId);

        if (latestPaymentOpt.isPresent()) {
            PayStatus payStatus = latestPaymentOpt.get().getPayStatus();

            if (payStatus != PayStatus.FAILED) {
                throw new BadRequestException(CANNOT_CREATE_PAYMENT.getMessage());
            }
        }

        String tempImpUid = "tempImpUid_" + System.currentTimeMillis();
        Payment payment = Payment.of(order, tempImpUid, PGProvider.KG, PaymentMethod.CARD);
        paymentRepository.save(payment);

        iamportClient.postPrepare(createPrepareData(payment));

        return PaymentResponse.of(payment);
    }

    /* 결제 요청 정보 전달 */
    @Transactional(readOnly = true)
    public PaymentRequest findPaymentRequestData(Long paymentId) {
        Payment payment = findByIdOrElseThrow(paymentId);

        if (payment.getPayStatus() != PayStatus.READY) {
            throw new BadRequestException(CANNOT_REQUEST_PAYMENT.getMessage());
        }

        String orderName = payment.getOrder().getName() + Instant.now();

        return PaymentRequest.of(payment, orderName);
    }

    /* 결제 요청 검증 */
    @Transactional
    public PaymentResponse verifyPayment(
            AuthUser authUser,
            PaymentVerificationRequest verificationDto) throws IamportResponseException, IOException {

        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse =
                iamportClient.paymentByImpUid(verificationDto.impUid());
        com.siot.IamportRestClient.response.Payment iamportPayment = iamportResponse.getResponse();

        Payment payment = findByMerchantUidOrElseThrow(verificationDto.merchantUid());
        Order order = payment.getOrder();

        if (!authUser.getUserId().equals(order.getUsers().getId())) {
            throw new ForbiddenException(NOT_OWN_PAYMENT.getMessage());
        }

        if (iamportPayment.getAmount().equals(verificationDto.amount())) {
            // 주문 승인
            payment.updateSuccessPayment(verificationDto.impUid());
            order.updatePayment(PaymentStatus.PAID);
            order.updateOrder(OrderStatus.ORDERED);
        } else {
            payment.updateFailPayment(verificationDto.impUid());
            order.updatePayment(PaymentStatus.FAILED);
        }
        return PaymentResponse.of(payment);
    }

    /* 결제 조회 */
    @Transactional(readOnly = true)
    public PaymentResponse findPaymentById(AuthUser authUser, Long paymentId) {
        Payment payment = findByIdOrElseThrow(paymentId);

        if (!authUser.getUserId().equals(payment.getOrder().getUsers().getId())) {
            throw new ForbiddenException(NOT_OWN_PAYMENT.getMessage());
        }
        return PaymentResponse.of(payment);
    }

    /* 결제 취소 */
    @Transactional
    public void cancelPayment(AuthUser authUser, Long paymentId) throws IamportResponseException, IOException {
        Payment payment = findByIdOrElseThrow(paymentId);
        Order order = payment.getOrder();

        if (!authUser.getUserId().equals(order.getUsers().getId())) {
            throw new ForbiddenException(NOT_OWN_PAYMENT.getMessage());
        }

        if (payment.getPayStatus() != PayStatus.PAID) {
            throw new BadRequestException(CANNOT_CANCEL_PAYMENT.getMessage());
        }

        // 전액 환불(결제 취소)
        CancelData cancelData = new CancelData(payment.getImpUid(), true);
        IamportResponse<com.siot.IamportRestClient.response.Payment> cancelResponse =
                iamportClient.cancelPaymentByImpUid(cancelData);

        if (cancelResponse.getResponse() == null) {
            throw new RuntimeException(CANCEL_PAYMENT_FAILED.getMessage());
        }

        payment.updateCancelPayment();
        order.updatePayment(PaymentStatus.CANCELLED);
        order.updateOrder(OrderStatus.CANCELLED);
    }


    private Payment findByMerchantUidOrElseThrow(String merchantUid) {
        return paymentRepository.findByMerchantUid(merchantUid).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_PAYMENT.getMessage()));
    }

    private Payment findByIdOrElseThrow(Long paymentId) {
        return paymentRepository.findById(paymentId).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_PAYMENT.getMessage())
        );
    }

    private PrepareData createPrepareData(Payment payment) {
        return new PrepareData(payment.getMerchantUid(),payment.getAmount());
    }

    private boolean canCreatePayment(Order order) {
        return order.getOrderStatus() == OrderStatus.PENDING
                && (order.getPaymentStatus() == PaymentStatus.PENDING || order.getPaymentStatus() == PaymentStatus.FAILED)
                && order.getDeliveryStatus() == DeliveryStatus.READY;
    }

    public Optional<Payment> getTopByOrderIdOrderByCreatedAtDesc(Long orderId) {
        return paymentRepository.findTopByOrderIdOrderByCreatedAtDesc(orderId);
    }
}