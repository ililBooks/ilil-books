package com.example.ililbooks.domain.order.controller;

import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.dto.response.OrdersGetResponse;
import com.example.ililbooks.domain.order.service.OrderService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    @Secured({USER})
    @PostMapping
    public Response<OrderResponse> createOrder(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Response.of(orderService.createOrder(authUser, pageNum, pageSize));
    }

    /* 주문 단건 조회 */
    @Secured({USER})
    @GetMapping("/{orderId}")
    public Response<OrderResponse> getOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Response.of(orderService.getOrder(authUser, orderId, pageNum, pageSize));
    }

    /* 주문 다건 조회 */
    @Secured({USER})
    @GetMapping
    public Response<Page<OrdersGetResponse>> getOrders(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Response.of(orderService.getOrders(authUser, pageNum, pageSize));
    }

    /* 주문 취소 */
    @Secured({USER})
    @PatchMapping("/{orderId}/cancel")
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
    @PatchMapping("/{orderId}/order")
    public Response<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Response.of(orderService.updateOrderStatus(authUser, orderId, pageNum, pageSize));
    }

    /* 배송 상태 변경 (배송 대기 -> 배송 중 -> 배송완료+주문완료) */
    @Secured({ADMIN})
    @PatchMapping("/{orderId}/delivery")
    public Response<OrderResponse> updateDeliveryStatus(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Response.of(orderService.updateDeliveryStatus(orderId, pageNum, pageSize));
    }
}