package com.example.ililbooks.domain.order.controller;

import com.example.ililbooks.domain.order.dto.request.OrderLimitedRequest;
import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.dto.response.OrdersGetResponse;
import com.example.ililbooks.domain.order.service.OrderDeliveryService;
import com.example.ililbooks.domain.order.service.OrderReadService;
import com.example.ililbooks.domain.order.service.OrderService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    private final OrderReadService orderReadService;
    private final OrderDeliveryService orderDeliveryService;

    /* 주문 생성 - 일반판 */
    @Operation(summary = "주문 생성", description = "장바구니에 담은 책들을 주문할 수 있습니다.")
    @Secured(USER)
    @PostMapping("/regular")
    public Response<OrderResponse> createOrder(
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable
    ) {
        return Response.of(orderService.createOrder(authUser, pageable));
    }

    /* 주문 생성 - 한정판 */
    @Operation(summary = "성공한 예약에 대한 주문 생성", description = "예약에 성공한 한정판 책을 주문할 수 있습니다.")
    @Secured(USER)
    @PostMapping("/limited")
    public Response<OrderResponse> createOrderForReservation(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody OrderLimitedRequest orderLimitedRequest,
            Pageable pageable
    ) {
        return Response.of(orderService.createOrderFromReservation(authUser, orderLimitedRequest.reservationId(), pageable));
    }

    /* 주문 단건 조회 */
    @Operation(summary = "주문 단건 조회", description = "주문 내역을 포함한 주문에 대한 정보를 단건으로 조회한다.")
    @Secured(USER)
    @GetMapping("/{orderId}")
    public Response<OrderResponse> findOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable
    ) {
        return Response.of(orderReadService.findOrder(authUser, orderId, pageable));
    }

    /* 주문 다건 조회 */
    @Operation(summary = "주문 다건 조회", description = "주문에 대한 정보를 다건으로 조회한다.")
    @Secured(USER)
    @GetMapping("/all")
    public Response<Page<OrdersGetResponse>> getOrders(
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable
    ) {
        return Response.of(orderReadService.getOrders(authUser, pageable));
    }

    /* 주문 취소 */
    @Operation(summary = "주문 취소", description = "배송이 되지 않은 주문을 취소할 수 있습니다.")
    @Secured(USER)
    @DeleteMapping("/cancel/{orderId}")
    public Response<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable
    ) {
        return Response.of(orderService.cancelOrder(authUser, orderId, pageable));
    }

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