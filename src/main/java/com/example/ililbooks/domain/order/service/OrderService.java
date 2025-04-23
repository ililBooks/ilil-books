package com.example.ililbooks.domain.order.service;

import com.example.ililbooks.domain.book.service.BookStockService;
import com.example.ililbooks.domain.cart.entity.Cart;
import com.example.ililbooks.domain.cart.entity.CartItem;
import com.example.ililbooks.domain.cart.service.CartService;
import com.example.ililbooks.domain.order.dto.response.OrderHistoryResponse;
import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.enums.OrderStatus;
import com.example.ililbooks.domain.order.repository.OrderRepository;
import com.example.ililbooks.domain.bestseller.service.BestSellerService;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.ForbiddenException;
import com.example.ililbooks.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final OrderHistoryService orderHistoryService;
    private final BookStockService bookStockService;
    private final BestSellerService bestSellerService;

    /* 주문 생성 */
    @Transactional
    public OrderResponse createOrder(AuthUser authUser, Pageable pageable) {

        Cart cart = cartService.findByUserIdOrElseNewCart(authUser.getUserId());

        if (cart.getItems().isEmpty()) {
            throw new NotFoundException(NOT_EXIST_SHOPPING_CART.getMessage());
        }

        Map<Long, CartItem> cartItemMap = cart.getItems();

        decreaseStocks(cartItemMap);

        BigDecimal totalPrice = calculateTotalPrice(cartItemMap);

        Order order = Order.of(Users.fromAuthUser(authUser), totalPrice);
        orderRepository.save(order);

        orderHistoryService.saveOrderHistory(cartItemMap, order);

        cartService.clearCart(authUser);

        bestSellerService.increaseBookSalesByQuantity(cartItemMap);

        return getOrderResponse(order, pageable);
    }

    /* 주문 상태 변경(취소) */
    @Transactional
    public OrderResponse cancelOrder(AuthUser authUser, Long orderId, Pageable pageable) {
        Order order = findByIdOrElseThrow(orderId);

        if (!authUser.getUserId().equals(order.getUsers().getId())) {
            throw new ForbiddenException(NOT_OWN_ORDER.getMessage());
        }

        if (!canCancelOrder(order)) {
            throw new BadRequestException(CANNOT_CANCEL_ORDER.getMessage());
        }

        order.updateOrder(OrderStatus.CANCELLED);

        rollbackStocks(order);
        return getOrderResponse(order, pageable);
    }

    /* 주문 상태 변경(승인) */
    @Transactional
    public OrderResponse updateOrderStatus(AuthUser authUser, Long orderId, Pageable pageable) {
        Order order = findByIdOrElseThrow(orderId);

        if (!authUser.getUserId().equals(order.getUsers().getId())) {
            throw new ForbiddenException(NOT_OWN_ORDER.getMessage());
        }

        if (!order.getOrderStatus().equals(OrderStatus.PENDING)) {
            throw new BadRequestException(CANNOT_CHANGE_ORDER.getMessage());
        }

        order.updateOrder(OrderStatus.ORDERED);
        return getOrderResponse(order, pageable);
    }

    /* 주문 총 가격 계산 */
    private BigDecimal calculateTotalPrice(Map<Long, CartItem> cartItemMap) {
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem cartItem : cartItemMap.values()) {
            BigDecimal itemPrice = cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalPrice = totalPrice.add(itemPrice);
        }
        return totalPrice;
    }

    /* 재고 감소 */
    private void decreaseStocks(Map<Long, CartItem> cartItemMap) {
        for (CartItem cartItem : cartItemMap.values()) {
            bookStockService.decreaseStock(cartItem.getBookId(), cartItem.getQuantity());
        }
    }

    /* 취소 시 재고 감소 롤백 */
    private void rollbackStocks(Order order) {
        List<CartItem> cartItemList = orderHistoryService.getCartItemListByOrderId(order.getId());

        for (CartItem cartItem : cartItemList) {
            bookStockService.rollbackStock(cartItem.getBookId(), cartItem.getQuantity());
        }
    }

    public OrderResponse getOrderResponse(Order order, Pageable pageable) {
        Page<OrderHistoryResponse> orderHistories = orderHistoryService.getOrderHistories(order.getId(), pageable);

        return OrderResponse.of(order, orderHistories);
    }

    private boolean canCancelOrder(Order order) {
        return order.getOrderStatus().canCancel() && order.getDeliveryStatus().canCancel();
    }

    public Order findByIdOrElseThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow( () -> new NotFoundException(NOT_FOUND_ORDER.getMessage()));
    }
}