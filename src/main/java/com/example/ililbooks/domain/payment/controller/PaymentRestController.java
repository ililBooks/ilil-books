package com.example.ililbooks.domain.payment.controller;

import com.example.ililbooks.domain.payment.dto.request.PaymentVerificationRequest;
import com.example.ililbooks.domain.payment.dto.response.PaymentResponse;
import com.example.ililbooks.domain.payment.service.PaymentService;
import com.example.ililbooks.global.dto.response.Response;
import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentRestController {

    private final PaymentService paymentService;

    /* 결제 준비 */
    @PostMapping("/prepare/{orderId}")
    public Response<PaymentResponse> preparePayment(@PathVariable Long orderId) throws IamportResponseException, IOException {
        return Response.of(paymentService.prepareOrder(orderId));
    }

    /* 결제 검증 */
    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(@RequestBody PaymentVerificationRequest verificationDto) {
        try {
            // 결제 검증 처리
            paymentService.verifyPayment(verificationDto);

            // 결제 성공 응답
            return ResponseEntity.ok().body("결제 검증 성공");

        } catch (IllegalArgumentException e) {
            // 금액 불일치 예외 처리
            return ResponseEntity.badRequest().body("결제 금액 불일치");
        } catch (Exception e) {
            // 기타 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("결제 검증 중 오류 발생");
        }
    }
}
