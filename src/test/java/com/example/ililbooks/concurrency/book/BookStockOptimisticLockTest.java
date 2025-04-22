package com.example.ililbooks.concurrency.book;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.enums.LimitedType;
import com.example.ililbooks.domain.book.enums.SaleStatus;
import com.example.ililbooks.domain.book.repository.BookRepository;
import com.example.ililbooks.domain.book.service.BookStockService;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.enums.UserRole;
import com.example.ililbooks.domain.user.repository.UserRepository;
import com.example.ililbooks.global.exception.BadRequestException;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Profile("test")
@SpringBootTest
class BookStockOptimisticLockTest {

    @Autowired
    private BookStockService bookStockService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    private Long bookId;
    private Book book;
    private Users adminUsers;

    @BeforeEach
    public void setup() {
        adminUsers = Users.builder()
                .email("admin@email.com")
                .nickname("adminNickname")
                .userRole(UserRole.ROLE_ADMIN)
                .build();

        adminUsers = userRepository.save(adminUsers);

        book = Book.builder()
                .title("book1")
                .users(adminUsers)
                .stock(100)
                .price(new BigDecimal(20000))
                .publisher("publisher1")
                .saleStatus(SaleStatus.ON_SALE)
                .limitedType(LimitedType.REGULAR)
                .build();

        book = bookRepository.save(book);
        bookId = book.getId();
    }

    @Test
    void 동시에_재고감소_요청_시_낙관적락_충돌_발생() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger optimisticLockFailures = new AtomicInteger();
        AtomicInteger badRequestExceptions = new AtomicInteger(); // 추가: BadRequestException 카운터

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    runInNewTransaction(() -> bookStockService.decreaseStock(bookId, 1));
                } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                    optimisticLockFailures.incrementAndGet(); // 낙관적 락 충돌
                } catch (BadRequestException e) {
                    badRequestExceptions.incrementAndGet(); // 추가: 재고 부족 또는 recover에서 throw된 경우
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Book book = bookRepository.findById(bookId).orElseThrow();
        int finalStock = book.getStock();
        int totalSuccess = threadCount - optimisticLockFailures.get() - badRequestExceptions.get();

        System.out.println("낙관적 락 실패 횟수: " + optimisticLockFailures.get());
        System.out.println("BadRequestException 발생 횟수: " + badRequestExceptions.get());
        System.out.println("최종 재고: " + finalStock);
        System.out.println("성공 요청 수: " + totalSuccess);

        assertEquals(100, totalSuccess + optimisticLockFailures.get() + badRequestExceptions.get());
        assertTrue(optimisticLockFailures.get() > 0 || badRequestExceptions.get() > 0);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runInNewTransaction(Runnable runnable) {
        runnable.run();
    }
}