package com.example.ililbooks.domain.order.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.service.BookService;
import com.example.ililbooks.domain.book.service.BookStokeService;
import com.example.ililbooks.domain.cart.entity.Cart;
import com.example.ililbooks.domain.cart.entity.CartItem;
import com.example.ililbooks.domain.cart.service.CartService;
import com.example.ililbooks.domain.order.dto.response.OrderHistoryResponse;
import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.enums.OrderStatus;
import com.example.ililbooks.domain.order.repository.OrderRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.ForbiddenException;
import com.example.ililbooks.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final OrderHistoryService orderHistoryService;
    private final BookService bookService;
    private final BookStokeService bookStokeService;

    /* 주문 생성 */
    @Transactional
    public OrderResponse createOrder(AuthUser authUser, int pageNum, int pageSize) {

        Cart cart = cartService.findByUserIdOrElseNewCart(authUser.getUserId());

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException(NOT_EXIST_SHOPPING_CART.getMessage());
        }

        Map<Long, Book> bookMap = getBookMap(cart);

        decreaseStocks(bookMap, cart);

        BigDecimal totalPrice = calculateTotalPrice(bookMap, cart);

        Order order = Order.of(Users.fromAuthUser(authUser), totalPrice);
        orderRepository.save(order);

        orderHistoryService.saveOrderHistory(bookMap, cart, order);

        cartService.clearCart(authUser);

        return getOrderResponse(order, pageNum, pageSize);
    }

    /* 주문 상태 변경(취소) */
    @Transactional
    public OrderResponse cancelOrder(AuthUser authUser, Long orderId, int pageNum, int pageSize) {
        Order order = findByIdOrElseThrow(orderId);

        if (!authUser.getUserId().equals(order.getUsers().getId())) {
            throw new ForbiddenException(NOT_OWN_ORDER.getMessage());
        }

        if (!isCanCancelOrder(order)) {
            throw new BadRequestException(CANNOT_CANCEL_ORDER.getMessage());
        }

        order.updateOrder(OrderStatus.CANCELLED);

        rollbackStocks(order);
        return getOrderResponse(order, pageNum, pageSize);
    }

    /* 주문 상태 변경(승인) */
    @Transactional
    public OrderResponse updateOrderStatus(AuthUser authUser, Long orderId, int pageNum, int pageSize) {
        Order order = findByIdOrElseThrow(orderId);

        if (!authUser.getUserId().equals(order.getUsers().getId())) {
            throw new ForbiddenException(NOT_OWN_ORDER.getMessage());
        }

        if (!order.getOrderStatus().equals(OrderStatus.PENDING)) {
            throw new BadRequestException(CANNOT_CHANGE_ORDER.getMessage());
        }

        order.updateOrder(OrderStatus.ORDERED);
        return getOrderResponse(order, pageNum, pageSize);
    }

    /* 주문 총 가격 계산 */
    private BigDecimal calculateTotalPrice(Map<Long, Book> bookMap, Cart cart) {
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (Book book : bookMap.values()) {
            CartItem cartItem = cart.getItems().get(book.getId());
            BigDecimal itemPrice = book.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalPrice = totalPrice.add(itemPrice);
        }
        return totalPrice;
    }

    /* 재고 감소 */
    private void decreaseStocks(Map<Long, Book> bookMap, Cart cart) {
        for (Book book : bookMap.values()) {
            CartItem cartItem = cart.getItems().get(book.getId());
            bookStokeService.decreaseStock(book, cartItem.getQuantity());
        }
    }

    /* 취소 시 재고 감소 롤백 */
    private void rollbackStocks(Order order) {
        List<CartItem> cartItemList = orderHistoryService.getCartItemListByOrderId(order.getId());

        for (CartItem cartItem : cartItemList) {
            Book book = bookService.findBookByIdOrElseThrow(cartItem.getBookId());
            bookStokeService.rollbackStock(book, cartItem.getQuantity());
        }
    }

    /* 책 검증 및 추출 */
    private Map<Long, Book> getBookMap(Cart cart) {
        return cart.getItems().keySet().stream()
                .collect(Collectors.toMap(
                        id -> id,
                        bookService::findBookByIdOrElseThrow
                ));
    }

    public OrderResponse getOrderResponse(Order order, int pageNum, int pageSize) {
        Page<OrderHistoryResponse> orderHistories = orderHistoryService.getOrderHistories(order.getId(), pageNum, pageSize);

        return OrderResponse.of(order, orderHistories);
    }

    private boolean isCanCancelOrder(Order order) {
        return order.getOrderStatus().isCanCancel() && order.getDeliveryStatus().isCanCancel();
    }

    public Order findByIdOrElseThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow( () -> new NotFoundException(NOT_FOUND_ORDER.getMessage()));
    }
}