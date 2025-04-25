package com.example.ililbooks.domain.order.service;

import com.example.ililbooks.domain.book.service.BookStockService;
import com.example.ililbooks.domain.cart.entity.CartItem;
import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.enums.OrderStatus;
import com.example.ililbooks.domain.payment.entity.Payment;
import com.example.ililbooks.domain.payment.enums.PayStatus;
import com.example.ililbooks.domain.payment.service.PaymentService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.ForbiddenException;
import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.example.ililbooks.global.exception.ErrorMessage.CANNOT_CANCEL_ORDER;
import static com.example.ililbooks.global.exception.ErrorMessage.NOT_OWN_ORDER;

@Service
@RequiredArgsConstructor
public class OrderCancelService {

    private final OrderHistoryService orderHistoryService;
    private final BookStockService bookStockService;
    private final PaymentService paymentService;
    private final OrderService orderService;

    /* 주문 상태 취소 및 결제 취소 */
    @Transactional
    public OrderResponse cancelOrder(AuthUser authUser, Long orderId, Pageable pageable) throws IamportResponseException, IOException {
        Order order = orderService.findByIdOrElseThrow(orderId);

        if (!authUser.getUserId().equals(order.getUsers().getId())) {
            throw new ForbiddenException(NOT_OWN_ORDER.getMessage());
        }

        if (!canCancelOrder(order)) {
            throw new BadRequestException(CANNOT_CANCEL_ORDER.getMessage());
        }

        Optional<Payment> paymentOpt = paymentService.getTopByOrderIdOrderByCreatedAtDesc(orderId);

        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();

            if (payment.getPayStatus() == PayStatus.PAID) {
                paymentService.cancelPayment(authUser, payment.getId());
            }
        }

        order.updateOrder(OrderStatus.CANCELLED);

        rollbackStocks(order);
        return orderService.getOrderResponse(order, pageable);
    }

    private boolean canCancelOrder(Order order) {
        return order.getOrderStatus().canCancel() && order.getDeliveryStatus().canCancel()
                && order.getPaymentStatus().canCancel() && order.getLimitedType().canCancel();
    }

    /* 취소 시 재고 감소 롤백 */
    private void rollbackStocks(Order order) {
        List<CartItem> cartItemList = orderHistoryService.getCartItemListByOrderId(order.getId());

        for (CartItem cartItem : cartItemList) {
            bookStockService.rollbackStock(cartItem.getBookId(), cartItem.getQuantity());
        }
    }
}
