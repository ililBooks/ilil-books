package com.example.ililbooks.domain.payment.controller;

import com.example.ililbooks.domain.payment.dto.request.PaymentRequest;
import com.example.ililbooks.domain.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment", description = "결제 관련 API")
public class PaymentController {

    private final PaymentService paymentService;

    /* 결제 요청 정보 요청 */
    @Operation(summary = "결제 요청", description = "결제 정보를 찾아 결제창으로 정보를 전달합니다.")
    @GetMapping("/request/{paymentId}")
    public String findPaymentRequestData(@PathVariable Long paymentId, Model model) {
        PaymentRequest paymentRequest = paymentService.findPaymentRequestData(paymentId);
        model.addAttribute("paymentRequest", paymentRequest);
        return "payment";
    }
}
