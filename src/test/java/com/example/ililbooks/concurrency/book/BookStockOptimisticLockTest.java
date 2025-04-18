package com.example.ililbooks.concurrency.book;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.service.BookService;
import com.example.ililbooks.domain.book.service.BookStockService;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.UserRole;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BookStockOptimisticLockTest {

    @Autowired
    private BookStockService bookStockService;

    private Book book;
    private Users adminUsers;
    @Autowired
    private BookService bookService;

    @BeforeEach
    void setUp() {
        adminUsers = Users.builder()
                .email("admin@email.com")
                .nickname("adminNickname")
                .userRole(UserRole.ROLE_ADMIN)
                .build();

        book = Book.builder()
                .id(1L)
                .title("book1")
                .users(adminUsers)
                .stock(100)
                .price(new BigDecimal(20000))
                .publisher("publisher1")
                .build();
    }

    @Test
    void 재고_변경_동시요청_낙관적락_예외_발생() throws Exception {
        // given
        Book savedBook = bookService.findBookByIdOrElseThrow(1L);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        AtomicReference<Exception> exception1 = new AtomicReference<>();
        AtomicReference<Exception> exception2 = new AtomicReference<>();

        // when
        executor.submit(() -> {
            try {
                runInNewTransaction(() -> bookStockService.decreaseStock(savedBook, 1));
            } catch (Exception e) {
                exception1.set(e);
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                runInNewTransaction(() -> bookStockService.decreaseStock(savedBook, 1));
            } catch (Exception e) {
                exception2.set(e);
            } finally {
                latch.countDown();
            }
        });

        latch.await();

        // then
        int conflictCount = 0;
        if (exception1.get() instanceof OptimisticLockException) conflictCount++;
        if (exception2.get() instanceof OptimisticLockException) conflictCount++;

        assertThat(conflictCount).isEqualTo(1); // 둘 중 하나는 실패해야 정상
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runInNewTransaction(Runnable runnable) {
        runnable.run();
    }
}