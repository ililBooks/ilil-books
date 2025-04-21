//package com.example.ililbooks.domain.book.service;
//
//import com.example.ililbooks.global.exception.BadRequestException;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import static com.example.ililbooks.domain.book.service.BookReadServiceTest.TEST_BOOK;
//import static com.example.ililbooks.global.exception.ErrorMessage.OUT_OF_STOCK;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//@ExtendWith(MockitoExtension.class)
//class BookStockServiceTest {
//
//    @InjectMocks
//    private BookStockService bookStockService;
//
//    public static final int TEST_QUANTITY = 1;
//    public static final int TEST_STOCK = 50;
//
//    @Test
//    void 요청_수량보다_재고가_부족하여_재고_감소_실패() {
//        //given
//        ReflectionTestUtils.setField(TEST_BOOK, "stock", -1);
//
//        //when & then
//        assertThrows(BadRequestException.class,
//                () -> bookStockService.decreaseStock(TEST_BOOK.getId(), TEST_QUANTITY),
//                OUT_OF_STOCK.getMessage()
//        );
//    }
//
//    @Test
//    void 재고_감소_성공() {
//        //given
//        ReflectionTestUtils.setField(TEST_BOOK, "stock", TEST_STOCK);
//        int stock = TEST_BOOK.getStock();
//
//        //when
//        bookStockService.decreaseStock(TEST_BOOK.getId(), TEST_QUANTITY);
//
//        //when
//        assertEquals(stock - TEST_QUANTITY,TEST_BOOK.getStock());
//    }
//
//    @Test
//    void 재고_롤백_성공() {
//        //given
//        int stock = TEST_BOOK.getStock();
//
//        //when
//        bookStockService.rollbackStock(TEST_BOOK, TEST_QUANTITY);
//
//        //when
//        assertEquals(stock + TEST_QUANTITY, TEST_BOOK.getStock());
//    }
//}