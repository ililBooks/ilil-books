package com.example.ililbooks.domain.payment.service;

import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.service.OrderService;
import com.example.ililbooks.domain.payment.dto.request.PaymentRequest;
import com.example.ililbooks.domain.payment.dto.request.PaymentVerificationRequest;
import com.example.ililbooks.domain.payment.dto.response.PaymentResponse;
import com.example.ililbooks.domain.payment.entity.Payment;
import com.example.ililbooks.domain.payment.enums.PGProvider;
import com.example.ililbooks.domain.payment.enums.PayStatus;
import com.example.ililbooks.domain.payment.enums.PaymentMethod;
import com.example.ililbooks.domain.payment.repository.PaymentRepository;
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

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderService orderService;
    private final PaymentRepository paymentRepository;
    private final IamportClient iamportClient;

    /* 결제 준비 및 결제 정보 저장 */
    @Transactional
    public PaymentResponse prepareOrder(Long orderId) throws IamportResponseException, IOException {
        Order order = orderService.findByIdOrElseThrow(orderId);

        String tempImpUid = "tempImpUid_" + System.currentTimeMillis();
        Payment payment = Payment.of(order, tempImpUid, PGProvider.KG, PaymentMethod.CARD);
        paymentRepository.save(payment);

        // 아임포트 사전 검증 추가
        iamportClient.postPrepare(createPrepareData(payment));

        return PaymentResponse.of(payment);
    }

    /* 결제 요청 정보 전달 */
    @Transactional(readOnly = true)
    public PaymentRequest findPaymentRequestDataByOrderId(Long paymentId) {
        Payment payment = findByIdOrElseThrow(paymentId);

        String orderName = payment.getOrder().getName();

        return PaymentRequest.of(payment, orderName);
    }

    @Transactional
    public void verifyPayment(PaymentVerificationRequest verificationDto) throws IamportResponseException, IOException {
        // Iamport 결제 검증
        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse = iamportClient.paymentByImpUid(verificationDto.impUid());
        com.siot.IamportRestClient.response.Payment iamportPayment = iamportResponse.getResponse();

        if (iamportPayment.getAmount().equals(verificationDto.amount())) {
            // 결제 완료 처리
            Payment payment = findByMerchantUidOrElseThrow(verificationDto.merchantUid());

            payment.updateSuccessPayment(verificationDto.impUid(), PayStatus.PAID);
        } else {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }
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