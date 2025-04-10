package com.example.ililbooks.domain.book.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

import static com.example.ililbooks.global.dto.ValidationMessage.*;
import static com.example.ililbooks.global.dto.ValidationMessage.INVALID_STOCK;

@Getter
@AllArgsConstructor
public class BookUpdateRequest {

    @Schema(example = "월급쟁이 재테크 상식사전 : 소중한 내 월급을 모으고 불리기 위한 최적의 솔루션 66")
    @NotBlank(message = NOT_NULL_TITLE)
    private String title;

    @Schema(example = "우용표 지음")
    @NotBlank(message = NOT_NULL_AUTHOR)
    private String author;

    @Schema(example = "20000")
    @NotNull(message = NOT_NULL_PRICE)
    private BigDecimal price;

    @Schema(example = "사회과학")
    @NotNull(message = NOT_NULL_CATEGORY)
    private String category;

    @Schema(example = "10", minimum = "0")
    @NotNull(message = NOT_NULL_STOCK)
    @Min(value = 0, message = INVALID_STOCK)
    private int stock;

    @Schema(
            example = "UNAVAILABLE",
            allowableValues = {"ON_SALE", "UNAVAILABLE", "SOLD_OUT"}
    )
    @NotBlank(message = NOT_NULL_SALE_STATUS)
    private String saleStatus;

    @Schema(
            example = "REGULAR",
            allowableValues = {"REGULAR", "LIMITED"}
    )
    @NotBlank(message = NOT_NULL_LIMITED_TYPE)
    private String limitedType;
}
