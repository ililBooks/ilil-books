package com.example.ililbooks.domain.payment.controller;

import com.example.ililbooks.domain.payment.dto.request.PaymentRequest;
import com.example.ililbooks.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /* 결제 요청 */
    @GetMapping("/request/{paymentId}")
    public String getPaymentRequestData(@PathVariable Long paymentId, Model model) {
        PaymentRequest paymentRequest = paymentService.findPaymentRequestDataByOrderId(paymentId);
        model.addAttribute("paymentRequest", paymentRequest);
        return "payment";
    }
}
