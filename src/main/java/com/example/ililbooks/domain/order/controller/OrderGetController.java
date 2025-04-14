package com.example.ililbooks.domain.order.controller;

import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.dto.response.OrdersGetResponse;
import com.example.ililbooks.domain.order.service.OrderGetService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.USER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@Tag(name = "OrderGet", description = "주문 조회 관련 API")
public class OrderGetController {

    private final OrderGetService orderGetService;

    /* 주문 단건 조회 */
    @Secured(USER)
    @GetMapping("/{orderId}")
    @Operation(summary = "주문 단건 조회", description = "주문 내역을 포함한 주문에 대한 정보를 단건으로 조회한다.")
    public Response<OrderResponse> findOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable
    ) {
        return Response.of(orderGetService.findOrder(authUser, orderId, pageable));
    }

    /* 주문 다건 조회 */
    @Secured(USER)
    @GetMapping("/all")
    @Operation(summary = "주문 다건 조회", description = "주문에 대한 정보를 다건으로 조회한다.")
    public Response<Page<OrdersGetResponse>> getOrders(
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable
    ) {
        return Response.of(orderGetService.getOrders(authUser, pageable));
    }

}
