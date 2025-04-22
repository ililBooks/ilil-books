package com.example.ililbooks.domain.book.service;

import com.example.ililbooks.domain.book.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookStockServiceTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookStockService bookStockService;

    private Long bookId;
    private Book book;

    @BeforeEach
    void setUp() {
        bookId = 1L;
        book = Mockito.mock(Book.class);
    }

    /* decreaseStock */
    @Test
    void decreaseStock_성공() {
        // given
        given(bookService.findBookByIdOrElseThrow(anyLong())).willReturn(book);

        // when
        bookStockService.decreaseStock(bookId, 3);

        // then
        verify(book, times(1)).decreaseStock(3);
    }

    /* rollbackStock */
    @Test
    void rollbackStock_성공() {
        // given
        given(bookService.findBookByIdOrElseThrow(anyLong())).willReturn(book);

        // when
        bookStockService.rollbackStock(bookId, 5);

        // then
        verify(book, times(1)).increaseStoke(5);
    }
}