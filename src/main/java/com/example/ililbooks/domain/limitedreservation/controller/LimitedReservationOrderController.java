package com.example.ililbooks.domain.limitedreservation.controller;

import com.example.ililbooks.domain.limitedreservation.dto.response.LimitedReservationOrderResponse;
import com.example.ililbooks.domain.limitedreservation.service.LimitedReservationOrderService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/limited-reservation-orders")
public class LimitedReservationOrderController {

    private final LimitedReservationOrderService limitedReservationOrderService;

    /*
     * 예약 기반 주문 생성
     * - 예약 상태 SUCCESS
     * - 재고 1 이상
     */
    @PostMapping("/{reservationId}")
    public Response<LimitedReservationOrderResponse> createOrderFromReservation(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable @NotNull Long reservationId
    ) {
        LimitedReservationOrderResponse response = limitedReservationOrderService.createFromReservation(authUser.getUserId(), reservationId);
        return Response.of(response);
    }
}
