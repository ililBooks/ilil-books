package com.example.ililbooks.domain.order.controller;

import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.dto.response.OrdersGetResponse;
import com.example.ililbooks.domain.order.service.OrderGetService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.USER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderGetController {

    private final OrderGetService orderGetService;

    /* 주문 단건 조회 */
    @Secured(USER)
    @GetMapping("/{orderId}")
    public Response<OrderResponse> findOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Response.of(orderGetService.findOrder(authUser, orderId, pageNum, pageSize));
    }

    /* 주문 다건 조회 */
    @Secured(USER)
    @GetMapping("/all")
    public Response<Page<OrdersGetResponse>> getOrders(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Response.of(orderGetService.getOrders(authUser, pageNum, pageSize));
    }

}
