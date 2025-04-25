package com.example.ililbooks.domain.payment.controller;

import com.example.ililbooks.domain.payment.dto.request.PaymentVerificationRequest;
import com.example.ililbooks.domain.payment.dto.response.PaymentResponse;
import com.example.ililbooks.domain.payment.enums.PayStatus;
import com.example.ililbooks.domain.payment.service.PaymentService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.MessageResponse;
import com.example.ililbooks.global.dto.response.Response;
import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentRestController {

    private final PaymentService paymentService;

    /* 결제 준비 */
    @PostMapping("/prepare/{orderId}")
    public Response<PaymentResponse> preparePayment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long orderId
    ) throws IamportResponseException, IOException {
        return Response.of(paymentService.prepareOrder(authUser, orderId));
    }

    /* 결제 검증 */
    @PostMapping("/verify")
    public MessageResponse<PaymentResponse> verifyPayment(
            @RequestBody PaymentVerificationRequest verificationDto) throws IamportResponseException, IOException {
        PaymentResponse paymentResponse = paymentService.verifyPayment(verificationDto);
        String message = PayStatus.PAID.name().equals(paymentResponse.payStatus()) ? "결제 성공" : "결제 실패";

        return MessageResponse.of(message, paymentResponse);
    }

    /* 결제 조회 */
    @GetMapping("/{paymentId}")
    public Response<PaymentResponse> findPaymentById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long paymentId
    ) {
        return Response.of(paymentService.findPaymentById(authUser, paymentId));
    }
}
