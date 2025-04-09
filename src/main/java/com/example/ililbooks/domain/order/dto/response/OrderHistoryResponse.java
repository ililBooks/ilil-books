package com.example.ililbooks.domain.order.dto.response;

import com.example.ililbooks.domain.order.entity.OrderHistory;
import lombok.Builder;

import java.math.BigDecimal;

public record OrderHistoryResponse(Long bookId, String title, String author, BigDecimal price, String limitedType,
                                   int quantity) {

    @Builder
    public OrderHistoryResponse {
    }

    public static OrderHistoryResponse of(OrderHistory orderHistory) {
        return OrderHistoryResponse.builder()
                .bookId(orderHistory.getBook().getId())
                .title(orderHistory.getTitle())
                .author(orderHistory.getAuthor())
                .price(orderHistory.getPrice())
                .limitedType(orderHistory.getLimitedType().name())
                .quantity(orderHistory.getQuantity())
                .build();
    }
}
