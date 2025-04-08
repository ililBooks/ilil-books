package com.example.ililbooks.domain.limitedreservation.controller;

import com.example.ililbooks.domain.limitedreservation.dto.request.LimitedReservationCreateRequest;
import com.example.ililbooks.domain.limitedreservation.dto.response.LimitedReservationResponse;
import com.example.ililbooks.domain.limitedreservation.service.LimitedReservationService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/limited-reservations")
public class LimitedReservationController {

    private final LimitedReservationService reservationService;

    /*/ 예약 생성 */
    @PostMapping
    public Response<LimitedReservationResponse> createReservation(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody LimitedReservationCreateRequest request
            ) {
        return Response.of(reservationService.createReservation(authUser, request));
    }

    /*/ 예약 단건 조회 */
    @GetMapping("/{reservationId}")
    public Response<LimitedReservationResponse> getMyReservation(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long reservationId
    ) {
        return Response.of(reservationService.getReservationByUser(authUser, reservationId));
    }

    /*/ 행사별 전체 예약 조회 */
    @GetMapping("/events/{eventId}")
    public Response<Page<LimitedReservationResponse>> getReservationsByEvent(
            @PathVariable Long eventId,
            Pageable pageable
    ) {
        return Response.of(reservationService.getReservationsByEvent(eventId, pageable));
    }

    // V2 - 관리자 조회용
//    @Secured({PUBLISHER, ADMIN})
//    @GetMapping("/events/{eventId}/status")
//    public Response<List<LimitedReservationResponse>> getReservationsByEventAndStatus(
//            @PathVariable Long eventId,
//            @RequestParam(name = "status") List<LimitedReservationStatus> statuses
//    ) {
//        return Response.of(reservationService.getReservationsByEventAndStatus(eventId, statuses));
//    }

    /*/ 예약 취소 */
    @PatchMapping("/{reservationId}/cancel")
    public Response<Void> cancelReservation(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long reservationId
    ) {
        reservationService.cancelReservation(authUser, reservationId);
        return Response.empty();
    }
}
