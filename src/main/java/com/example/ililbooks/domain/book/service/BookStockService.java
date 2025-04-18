package com.example.ililbooks.domain.book.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.global.exception.BadRequestException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.ililbooks.global.exception.ErrorMessage.OUT_OF_STOCK;
import static com.example.ililbooks.global.exception.ErrorMessage.STOCK_UPDATE_CONFLICT;

@Service
@RequiredArgsConstructor
public class BookStockService {

    @Retryable(
            retryFor = {
                    OptimisticLockException.class,
                    ObjectOptimisticLockingFailureException.class
            },
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public void decreaseStock(Book book, int quantity) {
        int remainingStock = book.decreaseStock(quantity);

        if (remainingStock < 0) {
            throw new BadRequestException(OUT_OF_STOCK.getMessage());
        }
    }

    @Retryable(
            retryFor = {
                    OptimisticLockException.class,
                    ObjectOptimisticLockingFailureException.class
            },
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public void rollbackStock(Book book, int quantity) {
        book.rollbackStock(quantity);
    }

    @Recover
    public void recover(OptimisticLockException e, Book book, int quantity) {
        throw new BadRequestException(STOCK_UPDATE_CONFLICT.getMessage());
    }
}
