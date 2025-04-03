package com.example.ililbooks.domain.limitedevent.controller;

import com.example.ililbooks.domain.limitedevent.dto.request.LimitedEventCreateRequest;
import com.example.ililbooks.domain.limitedevent.dto.request.LimitedEventUpdateRequest;
import com.example.ililbooks.domain.limitedevent.dto.response.LimitedEventResponse;
import com.example.ililbooks.domain.limitedevent.service.LimitedEventService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.dto.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @GetMapping("/{id}")
    public Response<LimitedEventResponse> getLimitedEvent(
            @PathVariable Long id
    ) {
        return Response.of(limitedEventService.getLimitedEvent(id));
    }

    /*/ 행사 다건 조회 */
    @GetMapping
    public Response<List<LimitedEventResponse>> getAllLimitedEvents() {
        return Response.of(limitedEventService.getAllLimitedEvents());
    }

    /*/ 행사 수정 (PUBLISHER 만 가능) */
    @Secured(PUBLISHER)
    @PatchMapping("/{id}")
    public Response<LimitedEventResponse> updateLimitedEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id,
            @Valid @RequestBody LimitedEventUpdateRequest request
    ) {
        return Response.of(limitedEventService.updateLimitedEvent(authUser, id, request));
    }

    /*/ 행사 삭지 (PUBLISHER 만 가능) */
    @Secured(PUBLISHER)
    @DeleteMapping
    public Response<Void> deleteLimitedEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long id
    ) {
        limitedEventService.deleteLimitedEvent(authUser, id);
        return Response.empty();
    }
}
