package com.example.ililbooks.domain.payment.service;

import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.service.OrderService;
import com.example.ililbooks.domain.payment.dto.request.PaymentRequest;
import com.example.ililbooks.domain.payment.dto.request.PaymentVerificationRequest;
import com.example.ililbooks.domain.payment.dto.response.PaymentResponse;
import com.example.ililbooks.domain.payment.entity.Payment;
import com.example.ililbooks.domain.payment.enums.PGProvider;
import com.example.ililbooks.domain.payment.enums.PaymentMethod;
import com.example.ililbooks.domain.payment.repository.PaymentRepository;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.ForbiddenException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.PrepareData;
import com.siot.IamportRestClient.response.IamportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static com.example.ililbooks.global.exception.ErrorMessage.NOT_FOUND_PAYMENT;
import static com.example.ililbooks.global.exception.ErrorMessage.NOT_OWN_ORDER;

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

        String orderName = payment.getOrder().getName();

        return PaymentRequest.of(payment, orderName);
    }

    /* 결제 조회 */
    @Transactional(readOnly = true)
    public PaymentResponse findPaymentById(AuthUser authUser, Long paymentId) {
        Payment payment = findByIdOrElseThrow(paymentId);

        if (!authUser.getUserId().equals(payment.getOrder().getUsers().getId())) {
            throw new ForbiddenException(NOT_OWN_ORDER.getMessage());
        }
        return PaymentResponse.of(payment);
    }

    /* 결제 요청 검증 */
    @Transactional
    public PaymentResponse verifyPayment(PaymentVerificationRequest verificationDto) throws IamportResponseException, IOException {
        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse =
                iamportClient.paymentByImpUid(verificationDto.impUid());
        com.siot.IamportRestClient.response.Payment iamportPayment = iamportResponse.getResponse();

        Payment payment = findByMerchantUidOrElseThrow(verificationDto.merchantUid());

        if (iamportPayment.getAmount().equals(verificationDto.amount())) {
            payment.updateSuccessPayment(verificationDto.impUid());
        } else {
            payment.updateFailPayment(verificationDto.impUid());
        }
        return PaymentResponse.of(payment);
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
}