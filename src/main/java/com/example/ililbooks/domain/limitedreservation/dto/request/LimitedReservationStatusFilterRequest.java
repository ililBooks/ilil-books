package com.example.ililbooks.domain.limitedreservation.dto.request;

import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LimitedReservationStatusFilterRequest {

    private Long eventId;
    private List<LimitedReservationStatus> statuses;
}
