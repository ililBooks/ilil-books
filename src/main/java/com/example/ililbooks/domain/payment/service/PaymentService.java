package com.example.ililbooks.domain.payment.service;

import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.service.OrderService;
import com.example.ililbooks.domain.payment.dto.request.PaymentRequest;
import com.example.ililbooks.domain.payment.entity.Payment;
import com.example.ililbooks.domain.payment.enums.PGProvider;
import com.example.ililbooks.domain.payment.enums.PaymentMethod;
import com.example.ililbooks.domain.payment.repository.PaymentRepository;
import com.example.ililbooks.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.ililbooks.global.exception.ErrorMessage.NOT_FOUND_PAYMENT;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderService orderService;
    private final PaymentRepository paymentRepository;

    /* 결제 준비 및 결제 정보 저장 */
    @Transactional
    public Long prepareOrder(Long orderId) {
        Order order = orderService.findByIdOrElseThrow(orderId);

        String TempImpUid = "Imp_" + System.currentTimeMillis();
        Payment payment = Payment.of(order, TempImpUid, PGProvider.KG, PaymentMethod.CARD);
        paymentRepository.save(payment);

        return order.getId();
    }

    /* 결제 요청 정보 전달 */
    @Transactional(readOnly = true)
    public PaymentRequest findPaymentRequestDataByOrderId(Long orderId) {
        Payment payment = findByOrderIdOrElseThrow(orderId);

        String orderName = "OrderNumber_" + payment.getMerchantUid();

        return PaymentRequest.of(payment, orderName);
    }

    private Payment findByOrderIdOrElseThrow(Long orderId) {
        return paymentRepository.findByOrderId(orderId).orElseThrow(
                () -> new NotFoundException(NOT_FOUND_PAYMENT.getMessage())
        );
    }
}