package com.example.ililbooks.domain.order.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.cart.entity.Cart;
import com.example.ililbooks.domain.cart.entity.CartItem;
import com.example.ililbooks.domain.order.dto.response.OrderHistoryResponse;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.entity.OrderHistory;
import com.example.ililbooks.domain.order.repository.OrderHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderHistoryService {

    private final OrderHistoryRepository orderHistoryRepository;

    /* 주문 내역 저장 */
    public void saveOrderHistory(Map<Long, Book> bookMap, Cart cart, Order order) {
        for (Book book : bookMap.values()) {
            CartItem cartItem = cart.getItems().get(book.getId());
            OrderHistory orderHistory = OrderHistory.of(order, book, cartItem.getQuantity());
            orderHistoryRepository.save(orderHistory);
        }
    }

    /* 주문 내역 조회 */
    public Page<OrderHistoryResponse> getOrderHistories(Long orderId, Pageable pageable) {
        Page<OrderHistory> findOrderHistories = orderHistoryRepository.findAllByOrderId(orderId, pageable);

        return findOrderHistories.map(OrderHistoryResponse::of);
    }

    /* 주문 내역 책 조회 */
    public List<CartItem> getCartItemListByOrderId(Long orderId) {
        List<OrderHistory> orderHistoryList = orderHistoryRepository.findAllByOrderId(orderId);

        return orderHistoryList.stream()
                .map(orderHistory -> CartItem.of(orderHistory.getBook().getId(), orderHistory.getQuantity()))
                .toList();
    }
}
