package com.example.ililbooks.domain.order.controller;

import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.service.OrderService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    /* 주문 생성 */
    @Secured(USER)
    @PostMapping
    public Response<OrderResponse> createOrder(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Response.of(orderService.createOrder(authUser, pageNum, pageSize));
    }

    /* 주문 취소 */
    @Secured(USER)
    @PatchMapping("/cancel/{orderId}")
    public Response<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Response.of(orderService.cancelOrder(authUser, orderId, pageNum, pageSize));
    }

    /* 주문 상태 변경 (주문 대기 -> 주문 완료)
    * todo: 추후 기술 고도화 결제에서 변경 예정 */
    @PatchMapping("/order/{orderId}")
    public Response<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Response.of(orderService.updateOrderStatus(authUser, orderId, pageNum, pageSize));
    }
}