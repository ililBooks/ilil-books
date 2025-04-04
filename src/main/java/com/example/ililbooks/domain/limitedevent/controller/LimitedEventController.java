package com.example.ililbooks.domain.limitedevent.controller;

import com.example.ililbooks.domain.limitedevent.dto.request.LimitedEventCreateRequest;
import com.example.ililbooks.domain.limitedevent.dto.request.LimitedEventUpdateRequest;
import com.example.ililbooks.domain.limitedevent.dto.response.LimitedEventResponse;
import com.example.ililbooks.domain.limitedevent.service.LimitedEventService;
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
import static com.example.ililbooks.domain.user.enums.UserRole.Authority.PUBLISHER;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/limited-events")
public class LimitedEventController {

    private final LimitedEventService limitedEventService;

    /*/ 행사 등록 (PUBLISHER 만 가능) */
    @Secured(PUBLISHER) // 향후 AuthPermission 으로 수정 여부 체크
    @PostMapping
    public Response<LimitedEventResponse> createEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody LimitedEventCreateRequest request
    ) {
        LimitedEventResponse response = limitedEventService.createLimitedEvent(authUser, request);
        return Response.of(response);
    }

    /*/ 행사 단건 조회 */
    @GetMapping("/{limitedEventId}")
    public Response<LimitedEventResponse> getLimitedEvent(
            @PathVariable Long limitedEventId
    ) {
        return Response.of(limitedEventService.getLimitedEvent(limitedEventId));
    }

    /*/ 행사 다건 조회 */
    @GetMapping
    public Response<Page<LimitedEventResponse>> getAllLimitedEvents(Pageable pageable) {
        return Response.of(limitedEventService.getAllLimitedEvents(pageable));
    }

    /*/ 행사 수정 (PUBLISHER 와 ADMIN 만 가능) */
    @Secured({PUBLISHER, ADMIN})
    @PatchMapping("/{limitedEventId}")
    public Response<LimitedEventResponse> updateLimitedEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long limitedEventId,
            @Valid @RequestBody LimitedEventUpdateRequest request
    ) {
        return Response.of(limitedEventService.updateLimitedEvent(authUser, limitedEventId, request));
    }

    /*/ 행사 삭제 (PUBLISHER 와 ADMIN 만 가능) */
    @Secured({PUBLISHER, ADMIN})
    @DeleteMapping("/{limitedEventId}")
    public Response<Void> deleteLimitedEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long limitedEventId
    ) {
        limitedEventService.deleteLimitedEvent(authUser, limitedEventId);
        return Response.empty();
    }
}
