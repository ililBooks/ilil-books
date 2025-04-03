package com.example.ililbooks.domain.limitedevent.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LimitedEventCreateRequest {

    @NotNull(message = "도서 ID는 필수입니다.")
    Long bookId;

    @NotBlank(message = "행사 제목은 필수입니다.")
    String title;

    @NotNull(message = "행사 시작일을 입력해주세요.")
    LocalDateTime startTime;

    @NotNull(message = "행사 종료일시를 입력해주세요.")
    @Future(message = "종료 일시는 미래 시간이어야 합니다.")
    LocalDateTime endTime;

    @NotBlank(message = "행사 설명을 입력해주세요.")
    String contents;

    @NotNull(message = "도서 수량을 입력해주세요.")
    Integer bookQuantity;
}
