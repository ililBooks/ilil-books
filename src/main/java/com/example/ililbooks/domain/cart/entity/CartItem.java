package com.example.ililbooks.domain.cart.entity;

import lombok.Builder;
import lombok.Getter;

//TODO 해당 도ㅔ인은 book ID만 저장하는 구조에요
// 만약 책 이름, 가격 등등 책 관련 정보를 가져오려면 join이 필요할거고 별도 조회하는 시점에 N+1이 생길수도있어요
// 필요할 것 같은 bookTitle, price, thumbnailUrl 등을 snapshot 으로 저장해보세요
@Getter
public class CartItem {

    private Long bookId;
    private int quantity;

    @Builder
    private CartItem(Long bookId, int quantity) {
        this.bookId = bookId;
        this.quantity = quantity;
    }

    public static CartItem of(Long bookId, int quantity) {
        return CartItem.builder()
                .bookId(bookId)
                .quantity(quantity)
                .build();
    }

    //TODO 이거 누적이라 명칭 바꾸면 좋음
    public void updateQuantity(int quantity) {
        this.quantity += quantity;
    }
}
