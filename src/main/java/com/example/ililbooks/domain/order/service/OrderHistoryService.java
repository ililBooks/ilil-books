package com.example.ililbooks.domain.order.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.cart.entity.Cart;
import com.example.ililbooks.domain.cart.entity.CartItem;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.entity.OrderHistory;
import com.example.ililbooks.domain.order.repository.OrderHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderHistoryService {

    private final OrderHistoryRepository orderHistoryRepository;

    public void saveOrderHistory(Map<Long, Book> bookMap, Cart cart, Order order) {
        for (Book book : bookMap.values()) {
            CartItem cartItem = cart.getItems().get(book.getId());
            OrderHistory orderHistory = OrderHistory.of(order, book, cartItem.getQuantity());
            orderHistoryRepository.save(orderHistory);
        }
    }
}
