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

import static com.example.ililbooks.global.exception.ErrorMessage.STOCK_UPDATE_CONFLICT;

@Service
@RequiredArgsConstructor
public class BookStockService {

    private final BookService bookService;

    @Retryable(
            retryFor = {
                    OptimisticLockException.class,
                    ObjectOptimisticLockingFailureException.class
            },
            noRetryFor = BadRequestException.class,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public void decreaseStock(Long bookId, int quantity) {
        Book book = bookService.findBookByIdOrElseThrow(bookId);
        book.decreaseStock(quantity);
    }

    @Retryable(
            retryFor = {
                    OptimisticLockException.class,
                    ObjectOptimisticLockingFailureException.class
            },
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public void rollbackStock(Long bookId, int quantity) {
        Book book = bookService.findBookByIdOrElseThrow(bookId);
        book.increaseStock(quantity);
    }

    @Recover
    public void recover(OptimisticLockException e, Long bookId, int quantity) {
        throw new BadRequestException(STOCK_UPDATE_CONFLICT.getMessage());
    }

    @Recover
    public void recover(ObjectOptimisticLockingFailureException e, Long bookId, int quantity) {
        throw new BadRequestException(STOCK_UPDATE_CONFLICT.getMessage());
    }

    @Recover
    public void recover(BadRequestException e, Long bookId, int quantity) {
        throw new BadRequestException(e.getMessage());
    }
}
