package com.example.ililbooks.domain.limitedevent.controller;

import com.example.ililbooks.domain.limitedevent.dto.request.LimitedEventCreateRequest;
import com.example.ililbooks.domain.limitedevent.dto.request.LimitedEventUpdateRequest;
import com.example.ililbooks.domain.limitedevent.dto.response.LimitedEventResponse;
import com.example.ililbooks.domain.limitedevent.service.LimitedEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
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
    public LimitedEventResponse createLimitedEvent(@Valid @RequestBody LimitedEventCreateRequest request) {
        return limitedEventService.createLimitedEvent(request);
    }

    /*/ 행사 단건 조회 */
    @GetMapping("/{id}")
    public LimitedEventResponse getLimitedEvent(@PathVariable Long id) {
        return limitedEventService.getLimitedEvent(id);
    }

    /*/ 행사 다건 조회 */
    @GetMapping
    public List<LimitedEventResponse> getAllLimitedEvents() {
        return limitedEventService.getAllLimitedEvents();
    }

    /*/ 행사 수정 (PUBLISHER 만 가능) */
    @Secured(PUBLISHER)
    @PatchMapping("/{id}")
    public LimitedEventResponse updateLimitedEvent(
            @PathVariable Long id,
            @Valid @RequestBody LimitedEventUpdateRequest request
            ) {
        return limitedEventService.updateLimitedEvent(id, request);
    }

    /*/ 행사 삭지 (PUBLISHER 만 가능) */
    @Secured(PUBLISHER)
    @DeleteMapping
    public void deleteLimitedEvent(@PathVariable Long id) {
        limitedEventService.deleteLimitedEvent(id);
    }
}
