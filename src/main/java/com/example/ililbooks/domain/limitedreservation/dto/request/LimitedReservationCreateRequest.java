package com.example.ililbooks.domain.limitedreservation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LimitedReservationCreateRequest {

    @NotNull(message = "예약할 한정판 행사 ID는 필수입니다.")
    private Long limitedEventId;
}
