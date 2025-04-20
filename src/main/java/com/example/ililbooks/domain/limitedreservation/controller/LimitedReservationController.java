package com.example.ililbooks.domain.limitedreservation.controller;

import com.example.ililbooks.domain.limitedreservation.dto.request.LimitedReservationCreateRequest;
import com.example.ililbooks.domain.limitedreservation.dto.request.LimitedReservationStatusFilterRequest;
import com.example.ililbooks.domain.limitedreservation.dto.response.LimitedReservationResponse;
import com.example.ililbooks.domain.limitedreservation.dto.response.LimitedReservationStatusHistoryResponse;
import com.example.ililbooks.domain.limitedreservation.dto.response.LimitedReservationStatusResponse;
import com.example.ililbooks.domain.limitedreservation.service.LimitedReservationOrderService;
import com.example.ililbooks.domain.limitedreservation.service.LimitedReservationQueryService;
import com.example.ililbooks.domain.limitedreservation.service.LimitedReservationService;
import com.example.ililbooks.domain.order.dto.response.OrdersGetResponse;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/limited-reservations")
public class LimitedReservationController {

    private final LimitedReservationService reservationService;
    private final LimitedReservationQueryService queryService;
    private final LimitedReservationOrderService orderService;

    /*/ 예약 생성 */
    @Secured(USER)
    @PostMapping
    public Response<LimitedReservationResponse> createReservation(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody LimitedReservationCreateRequest request
    ) {
        return Response.of(reservationService.createReservation(authUser, request));
    }

    /*/ 예약 단건 조회 */
    @Secured(USER)
    @GetMapping("/{reservationId}")
    public Response<LimitedReservationResponse> getMyReservation(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long reservationId
    ) {
        return Response.of(queryService.getReservationByUser(authUser, reservationId));
    }

    /*/ 행사별 전체 예약 조회 */
    @Secured({PUBLISHER, ADMIN})
    @GetMapping("/events/{eventId}")
    public Response<Page<LimitedReservationResponse>> getAllReservationsByEvent(
            @PathVariable Long eventId,
            Pageable pageable
    ) {
        return Response.of(queryService.getReservationsByEvent(eventId, pageable));
    }

     /*/ 예약 상태별 조회 */
    @Secured({PUBLISHER, ADMIN})
    @GetMapping("/events/status")
    public Response<List<LimitedReservationResponse>> getReservationsByEventAndStatus(
            @RequestBody LimitedReservationStatusFilterRequest request
    ) {
        return Response.of(queryService.getReservationsByEventAndStatus(request.eventId(), request.statuses()));
    }

    /*/ 예약 취소 */
    @Secured(USER)
    @PatchMapping("/cancel/{reservationId}")
    public Response<Void> cancelReservation(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long reservationId
    ) {
        reservationService.cancelReservation(authUser, reservationId);
        return Response.empty();
    }

    /*/ 주문 생성 */
    @Secured(USER)
    @PostMapping("/order/{reservationId}")
    public Response<OrdersGetResponse> createOrderForReservation(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long reservationId
    ) {
        return Response.of(orderService.createOrderFroReservation(authUser, reservationId));
    }

    /*/ 실시간 예약 상태 조회 */
    @Secured(USER)
    @GetMapping("/status/{reservationId}")
    public Response<LimitedReservationStatusResponse> getReservationStatus(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long reservationId
    ) {
        return Response.of(queryService.getReservationStatus(authUser, reservationId));
    }

    /*/ 예약 상태 변경 이력 조회 */
    @Secured({PUBLISHER, ADMIN})
    @GetMapping("/history/{reservationId}")
    public Response<List<LimitedReservationStatusHistoryResponse>> getReservationStatusHistory(
            @PathVariable Long reservationId
    ) {
        return Response.of(queryService.getReservationStatusHistory(reservationId));
    }

    @Secured({PUBLISHER, ADMIN})
    @PostMapping("/status/limitedEvents")
    public Response<List<LimitedReservationResponse>> getReservationsByEventAndStatusWithFilter(
            @RequestBody LimitedReservationStatusFilterRequest request
    ) {
        return Response.of(queryService.getReservationsByFilter(request));
    }
}
