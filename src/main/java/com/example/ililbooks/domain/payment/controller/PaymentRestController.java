package com.example.ililbooks.domain.payment.controller;

import com.example.ililbooks.domain.payment.dto.request.PaymentRequest;
import com.example.ililbooks.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentRestController {

    private final PaymentService paymentService;

    /**
     * 결제 준비: 결제 정보 미리 저장
     * - 프론트에서 "결제하기" 버튼 누르면 호출
     */
    @PostMapping("/prepare/{orderId}")
    public ResponseEntity<Long> preparePayment(@PathVariable Long orderId) {
        Long savedOrderId = paymentService.prepareOrder(orderId);
        return ResponseEntity.ok(savedOrderId);
    }
}
