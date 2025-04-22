package com.example.ililbooks.domain.order.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.service.BookService;
import com.example.ililbooks.domain.cart.entity.CartItem;
import com.example.ililbooks.domain.order.dto.response.OrderHistoryResponse;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.entity.OrderHistory;
import com.example.ililbooks.domain.order.repository.OrderHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderHistoryService {

    private final OrderHistoryRepository orderHistoryRepository;
    private final BookService bookService;

    /* 주문 내역 저장 */
    public void saveOrderHistory(Map<Long, CartItem> cartItemMap, Order order) {
        for (CartItem cartItem : cartItemMap.values()) {
            Book book = bookService.findBookByIdOrElseThrow(cartItem.getBookId());
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
                .map(orderHistory -> CartItem.of(orderHistory.getBook(), orderHistory.getQuantity()))
                .toList();
    }
}
