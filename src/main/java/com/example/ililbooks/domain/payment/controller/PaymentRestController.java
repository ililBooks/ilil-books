package com.example.ililbooks.domain.payment.controller;

import com.example.ililbooks.domain.payment.dto.request.PaymentOrderRequest;
import com.example.ililbooks.domain.payment.dto.request.PaymentVerificationRequest;
import com.example.ililbooks.domain.payment.dto.response.PaymentResponse;
import com.example.ililbooks.domain.payment.enums.PayStatus;
import com.example.ililbooks.domain.payment.service.PaymentService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.MessageResponse;
import com.example.ililbooks.global.dto.response.Response;
import com.siot.IamportRestClient.exception.IamportResponseException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.USER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment", description = "결제 관련 API")
public class PaymentRestController {

    private final PaymentService paymentService;

    /* 결제 준비 */
    @Operation(summary = "결제 준비", description = "주문 생성 후 결제 정보를 저장합니다.")
    @Secured(USER)
    @PostMapping("/prepare")
    public Response<PaymentResponse> preparePayment(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody PaymentOrderRequest paymentOrderRequest
    ) throws IamportResponseException, IOException {
        return Response.of(paymentService.prepareOrder(authUser, paymentOrderRequest.orderId()));
    }

    /* 결제 성공 실패 검증 및 주문 승인 */
    @Operation(summary = "결제 승인", description = "결제 요청을 바탕으로 결제 성공 및 실패를 판단합니다.")
    @Secured(USER)
    @PostMapping("/verify")
    public MessageResponse<PaymentResponse> verifyPayment(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody PaymentVerificationRequest verificationDto
    ) throws IamportResponseException, IOException {
        PaymentResponse paymentResponse = paymentService.verifyPayment(authUser, verificationDto);
        String message = PayStatus.PAID.name().equals(paymentResponse.payStatus()) ? "결제 성공" : "결제 실패";

        return MessageResponse.of(message, paymentResponse);
    }

    /* 결제 조회 */
    @Operation(summary = "결제 조회", description = "결제 상태를 조회할 수 있습니다.")
    @Secured(USER)
    @GetMapping("/{paymentId}")
    public Response<PaymentResponse> findPaymentById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long paymentId
    ) {
        return Response.of(paymentService.findPaymentById(authUser, paymentId));
    }
}
