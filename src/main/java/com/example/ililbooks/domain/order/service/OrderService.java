package com.example.ililbooks.domain.order.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.service.BookService;
import com.example.ililbooks.domain.book.service.BookStokeService;
import com.example.ililbooks.domain.cart.entity.Cart;
import com.example.ililbooks.domain.cart.entity.CartItem;
import com.example.ililbooks.domain.cart.service.CartService;
import com.example.ililbooks.domain.order.dto.response.OrderResponse;
import com.example.ililbooks.domain.order.entity.Order;
import com.example.ililbooks.domain.order.repository.OrderRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.example.ililbooks.global.exception.ErrorMessage.NOT_EXIST_SHOPPING_CART;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final BookService bookService;
    private final BookStokeService bookStokeService;

    @Transactional
    public OrderResponse createOrder(AuthUser authUser) {

        // todo 작성 마무리하기

        // 1. 재고 차감
        Cart cart = cartService.findByUserIdOrElseNewCart(authUser.getUserId());

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException(NOT_EXIST_SHOPPING_CART.getMessage());
        }

        for (CartItem cartItem : cart.getItems().values()) {
            bookStokeService.decreaseStock(cartItem.getBookId(), cartItem.getQuantity());
        }

        // 2. order 저장
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems().values()) {
            Book book = bookService.findBookByIdOrElseThrow(cartItem.getBookId());
            BigDecimal itemPrice = book.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalPrice = totalPrice.add(itemPrice);
        }

        Order order = Order.of(Users.fromAuthUser(authUser), totalPrice);
        orderRepository.save(order);

        // 3. orderHistory 저장

        // 4. 장바구니 비우기

        // 5. order 바인딩
        return OrderResponse.of(order);
    }
}
