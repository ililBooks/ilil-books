package com.example.ililbooks.domain.order.service;

import com.example.ililbooks.domain.bestseller.service.BestSellerService;
import com.example.ililbooks.domain.book.service.BookStockService;
import com.example.ililbooks.domain.cart.entity.Cart;
import com.example.ililbooks.domain.cart.entity.CartItem;
import com.example.ililbooks.domain.cart.service.CartService;
import com.example.ililbooks.domain.limitedevent.entity.LimitedEvent;
import com.example.ililbooks.domain.limitedreservation.entity.LimitedReservation;
import com.example.ililbooks.domain.limitedreservation.enums.LimitedReservationStatus;
import com.example.ililbooks.domain.limitedreservation.service.LimitedReservationReadService;
import com.example.ililbooks.domain.order.dto.response.OrderHistoryResponse;
import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.enums.LimitedType;
import com.example.ililbooks.domain.order.repository.OrderRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.NotFoundException;
import com.example.ililbooks.global.asynchronous.rabbitmq.dto.request.MessageOrderRequest;
import com.example.ililbooks.global.asynchronous.rabbitmq.service.RabbitMqService;
import com.example.ililbooks.global.notification.service.NotificationSesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final OrderHistoryService orderHistoryService;
    private final BookStockService bookStockService;
    private final LimitedReservationReadService limitedReservationReadService;
    private final BestSellerService bestSellerService;
    private final UserService userService;
    private final NotificationSesService notificationSesService;

    /* 주문 생성 - 일반판 */
    @Transactional
    public OrderResponse createOrder(AuthUser authUser, Pageable pageable) {

        Cart cart = cartService.findByUserIdOrElseNewCart(authUser.getUserId());

        if (cart.getItems().isEmpty()) {
            throw new NotFoundException(NOT_EXIST_SHOPPING_CART.getMessage());
        }

        Map<Long, CartItem> cartItemMap = cart.getItems();

        decreaseStocks(cartItemMap);

        BigDecimal totalPrice = calculateTotalPrice(cartItemMap);
        String orderName = generateOrderName(cartItemMap);

        Order order = Order.of(Users.fromAuthUser(authUser), orderName, totalPrice, LimitedType.REGULAR);
        orderRepository.save(order);

        orderHistoryService.saveOrderHistory(cartItemMap, order);

        cartService.clearCart(authUser);

        bestSellerService.increaseBookSalesByQuantity(cartItemMap);

        Users users = userService.findByIdOrElseThrow(authUser.getUserId());

        //알림 수신 동의인 경우
        if (users.isNotificationAgreed()) {
            notificationSesService.sendOrderMailWithAsync(authUser, order.getNumber(), order.getTotalPrice());
        }

        return getOrderResponse(order, pageable);
    }

    /* 주문 생성 - 한정판 */
    @Transactional
    public OrderResponse createOrderFromReservation(AuthUser authUser, Long reservationId, Pageable pageable) {

        LimitedReservation limitedReservation = validateReservation(authUser, reservationId);
        LimitedEvent limitedEvent = limitedReservation.getLimitedEvent();

        Map<Long, CartItem> cartItemMap = new HashMap<>();
        cartItemMap.put(limitedEvent.getBook().getId(), CartItem.of(limitedEvent.getBook(), 1));

        BigDecimal totalPrice = calculateTotalPrice(cartItemMap);
        String orderName = generateOrderName(cartItemMap);

        Order order = Order.of(Users.fromAuthUser(authUser), orderName, totalPrice, LimitedType.LIMITED);
        limitedReservation.linkOrder(order);
        orderRepository.save(order);

        orderHistoryService.saveOrderHistory(cartItemMap, order);

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

    public OrderResponse getOrderResponse(Order order, Pageable pageable) {
        Page<OrderHistoryResponse> orderHistories = orderHistoryService.getOrderHistories(order.getId(), pageable);

        return OrderResponse.of(order, orderHistories);
    }

    public Order findByIdOrElseThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow( () -> new NotFoundException(NOT_FOUND_ORDER.getMessage()));
    }

    private LimitedReservation validateReservation(AuthUser authUser, Long reservationId) {
        LimitedReservation limitedReservation = limitedReservationReadService.findReservationByIdOrElseThrow(reservationId);

        if (!limitedReservation.getUsers().getId().equals(authUser.getUserId())) {
            throw new BadRequestException(NO_PERMISSION.getMessage());
        }

        if (limitedReservation.getStatus() != LimitedReservationStatus.RESERVED) {
            throw new BadRequestException(RESERVATION_NOT_SUCCESS.getMessage());
        }

        if (limitedReservation.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException(RESERVATION_EXPIRED.getMessage());
        }

        if (limitedReservation.hasOrder()) {
            throw new BadRequestException(ALREADY_ORDERED.getMessage());
        }
        return limitedReservation;
    }

    private String generateOrderName(Map<Long, CartItem> cartItemMap) {
        int totalCount = cartItemMap.size();

        CartItem representativeItem = cartItemMap.entrySet()
                .stream()
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseThrow();

        String baseTitle = representativeItem.getTitle();
        int baseTitleLengthLimit = 30;

        if (totalCount == 1) {
            return baseTitle;
        }

        String trimmedTitle = baseTitle.length() > baseTitleLengthLimit
                ? baseTitle.substring(0, baseTitleLengthLimit) + "..."
                : baseTitle;

        return String.format("%s 외 %d권", trimmedTitle, totalCount - 1);
    }
}