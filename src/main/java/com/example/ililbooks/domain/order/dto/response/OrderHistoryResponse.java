package com.example.ililbooks.domain.order.dto.response;

import com.example.ililbooks.domain.order.entity.OrderHistory;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderHistoryResponse {

    private final Long bookId;

    private final String title;

    private final String author;

    private final BigDecimal price;

    private final String limitedType;

    private final int quantity;

    @Builder
    private OrderHistoryResponse(Long bookId, String title, String author, BigDecimal price, String limitedType, int quantity) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.price = price;
        this.limitedType = limitedType;
        this.quantity = quantity;
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
