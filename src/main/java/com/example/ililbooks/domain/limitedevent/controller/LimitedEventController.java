package com.example.ililbooks.domain.limitedevent.controller;

import com.example.ililbooks.domain.limitedevent.dto.request.LimitedEventCreateRequest;
import com.example.ililbooks.domain.limitedevent.dto.request.LimitedEventUpdateRequest;
import com.example.ililbooks.domain.limitedevent.dto.response.LimitedEventResponse;
import com.example.ililbooks.domain.limitedevent.service.LimitedEventService;
import com.example.ililbooks.domain.limitedreservation.dto.response.LimitedReservationSummaryResponse;
import com.example.ililbooks.domain.limitedreservation.service.LimitedReservationQueryService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.ililbooks.domain.user.enums.UserRole.Authority.ADMIN;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/limited-events")
public class LimitedEventController {

    private final LimitedEventService limitedEventService;
    private final LimitedReservationQueryService queryService;

    /*/ 행사 등록 (PUBLISHER 만 가능) */
    @Secured(ADMIN)
    @PostMapping
    public Response<LimitedEventResponse> createLimitedEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody LimitedEventCreateRequest request
    ) {
        return Response.of(limitedEventService.createLimitedEvent(authUser, request));
    }

    /*/ 행사 단건 조회 */
    @GetMapping("/{limitedEventId}")
    public Response<LimitedEventResponse> getLimitedEvent(@PathVariable Long limitedEventId) {
        return Response.of(limitedEventService.getLimitedEvent(limitedEventId));
    }

    /*/ 행사 다건 조회 */
    @GetMapping
    public Response<Page<LimitedEventResponse>> getAllLimitedEventList(Pageable pageable) {
        return Response.of(limitedEventService.getAllLimitedEvents(pageable));
    }

    /*/ 예약 통계 요약 조회 */
    @Secured({ADMIN})
    @GetMapping("/summary/{limitedEventId}")
    public Response<LimitedReservationSummaryResponse> getReservationSummary(
            @PathVariable Long limitedEventId
    ) {
        return Response.of(queryService.getReservationSummary(limitedEventId));
    }

    /*/ 행사 수정 (PUBLISHER 와 ADMIN 만 가능) */
    @Secured({ADMIN})
    @PatchMapping("/{limitedEventId}")
    public Response<LimitedEventResponse> updateLimitedEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long limitedEventId,
            @Valid @RequestBody LimitedEventUpdateRequest request
    ) {
        return Response.of(limitedEventService.updateLimitedEvent(authUser, limitedEventId, request));
    }

    /*/ 행사 삭제 (PUBLISHER 와 ADMIN 만 가능) */
    @Secured({ADMIN})
    @DeleteMapping("/delete/{limitedEventId}")
    public Response<Void> deleteLimitedEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long limitedEventId
    ) {
        limitedEventService.deleteLimitedEvent(authUser, limitedEventId);
        return Response.empty();
    }
}
