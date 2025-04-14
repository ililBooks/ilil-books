package com.example.ililbooks.domain.order.controller;

import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.service.OrderDeliveryService;
import com.example.ililbooks.global.dto.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.ADMIN;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@Tag(name = "OrderDelivery", description = "주문 배송 관련 API")
public class OrderDeliveryController {

    private final OrderDeliveryService orderDeliveryService;

    /* 배송 상태 변경 (배송 대기 -> 배송 중 -> 배송완료+주문완료) */
    @Operation(summary = "배송 상태 변경", description = "주문의 배송 상태를 배송 대기에서 배송 중으로 변경, 배송 중을 배송 완료 및 주문 완료로 변경합니다.")
    @Secured(ADMIN)
    @PatchMapping("/delivery/{orderId}")
    public Response<OrderResponse> updateDeliveryStatus(
            @PathVariable Long orderId,
            Pageable pageable
    ) {
        return Response.of(orderDeliveryService.updateDeliveryStatus(orderId, pageable));
    }
}
