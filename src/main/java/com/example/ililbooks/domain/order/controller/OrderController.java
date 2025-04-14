package com.example.ililbooks.domain.order.controller;

import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.service.OrderService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@Tag(name = "Order", description = "주문 관련 API")
public class OrderController {

    private final OrderService orderService;

    /* 주문 생성 */
    @Operation(summary = "주문 생성", description = "장바구니에 담은 책들을 주문할 수 있습니다.")
    @Secured(USER)
    @PostMapping
    public Response<OrderResponse> createOrder(
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable
    ) {
        return Response.of(orderService.createOrder(authUser, pageable));
    }

    /* 주문 취소 */
    @Secured(USER)
    @Operation(summary = "주문 취소", description = "배송이 되지 않은 주문을 취소할 수 있습니다.")
    @PatchMapping("/cancel/{orderId}")
    public Response<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable
    ) {
        return Response.of(orderService.cancelOrder(authUser, orderId, pageable));
    }

    /* 주문 상태 변경 (주문 대기 -> 주문 완료)
    * todo: 추후 기술 고도화 결제에서 변경 예정 */
    @Operation(summary = "주문 상태 변경", description = "주문 상태를 주문 대기에서 완료 상태로 변경할 수 있습니다.")
    @PatchMapping("/order/{orderId}")
    public Response<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable
    ) {
        return Response.of(orderService.updateOrderStatus(authUser, orderId, pageable));
    }
}