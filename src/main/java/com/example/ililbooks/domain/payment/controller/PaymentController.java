package com.example.ililbooks.domain.payment.controller;

import com.example.ililbooks.domain.payment.dto.request.PaymentRequest;
import com.example.ililbooks.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/payments")
//public class PaymentController {
//
//    private final PaymentService paymentService;
//
//    /**
//     * 결제 준비: 결제 정보 미리 저장
//     * - 프론트에서 "결제하기" 버튼 누르면 호출
//     */
//    @PostMapping("/prepare/{orderId}")
//    public ResponseEntity<Long> preparePayment(@PathVariable Long orderId) {
//        Long savedOrderId = paymentService.prepareOrder(orderId);
//        return ResponseEntity.ok(savedOrderId);
//    }
//
//    /**
//     * 결제 요청에 필요한 데이터 반환
//     * - 프론트에서 IMP.request_pay 호출 전에 이 정보로 데이터 구성
//     */
//    @GetMapping("/request/{orderId}")
//    public ResponseEntity<PaymentRequest> getPaymentRequestData(@PathVariable Long orderId) {
//        PaymentRequest paymentRequest = paymentService.findPaymentRequestDataByOrderId(orderId);
//        return ResponseEntity.ok(paymentRequest);
//    }
//}

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 페이지로 이동하면서 PaymentRequest 데이터를 심어줌
    @GetMapping("/request/{orderId}")
    public String getPaymentRequestData(@PathVariable Long orderId, Model model) {
        PaymentRequest paymentRequest = paymentService.findPaymentRequestDataByOrderId(orderId);
        model.addAttribute("paymentRequest", paymentRequest);
        return "payment"; // resources/templates/payment.html 렌더링 (타임리프 기준)
    }
}
