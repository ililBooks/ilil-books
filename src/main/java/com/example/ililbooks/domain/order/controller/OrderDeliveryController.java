package com.example.ililbooks.domain.order.controller;

import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.service.OrderDeliveryService;
import com.example.ililbooks.global.dto.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.ADMIN;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderDeliveryController {

    private final OrderDeliveryService orderDeliveryService;

    /* 배송 상태 변경 (배송 대기 -> 배송 중 -> 배송완료+주문완료) */
    @Secured(ADMIN)
    @PatchMapping("/delivery/{orderId}")
    public Response<OrderResponse> updateDeliveryStatus(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Response.of(orderDeliveryService.updateDeliveryStatus(orderId, pageNum, pageSize));
    }
}
