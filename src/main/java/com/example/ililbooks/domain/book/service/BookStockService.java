package com.example.ililbooks.domain.book.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.ililbooks.global.exception.ErrorMessage.OUT_OF_STOCK;

@Service
@RequiredArgsConstructor
public class BookStockService {

    public void decreaseStock(Book book, int quantity) {
        int remainingStock = book.decreaseStock(quantity);

        if (remainingStock < 0) {
            throw new BadRequestException(OUT_OF_STOCK.getMessage());
        }
    }

    public void rollbackStock(Book book, int quantity) {
        book.rollbackStock(quantity);
    }
}
