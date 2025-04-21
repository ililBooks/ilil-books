package com.example.ililbooks.concurrency.book;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.enums.LimitedType;
import com.example.ililbooks.domain.book.enums.SaleStatus;
import com.example.ililbooks.domain.book.repository.BookRepository;
import com.example.ililbooks.domain.book.service.BookService;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//@SpringBootTest
//class BookStockOptimisticLockTest {
//
//    @Autowired
//    private BookStockService bookStockService;
//
//    @Autowired
//    private BookRepository bookRepository;
//
//    @Autowired
//    private UserRepository usersRepository;
//
//    private Book book;
//    private Users adminUsers;
//    @Autowired
//    private BookService bookService;
//
//    @BeforeEach
//    void setUp() {
//        adminUsers = Users.builder()
//                .email("admin@email.com")
//                .nickname("adminNickname")
//                .userRole(UserRole.ROLE_ADMIN)
//                .build();
//
//        adminUsers = usersRepository.save(adminUsers);
//
//        book = Book.builder()
//                .title("book1")
//                .users(adminUsers)
//                .stock(100)
//                .price(new BigDecimal(20000))
//                .publisher("publisher1")
//                .saleStatus(SaleStatus.ON_SALE)
//                .limitedType(LimitedType.REGULAR)
//                .build();
//
//        book = bookRepository.save(book);
//    }
//
//    @Test
//    @Transactional
//    void 재고_변경_동시요청_낙관적락_예외_발생() throws Exception {
//        // given
//        ExecutorService executor = Executors.newFixedThreadPool(2);
//        CountDownLatch latch = new CountDownLatch(2);
//
//        AtomicReference<Exception> exception1 = new AtomicReference<>();
//        AtomicReference<Exception> exception2 = new AtomicReference<>();
//
//        // when
//        executor.submit(() -> {
//            try {
//                runInNewTransaction(() -> bookStockService.decreaseStock(book.getId(), 1));
//            } catch (Exception e) {
//                exception1.set(e);
//            } finally {
//                latch.countDown();
//            }
//        });
//
//        executor.submit(() -> {
//            try {
//                runInNewTransaction(() -> bookStockService.decreaseStock(book.getId(), 1));
//            } catch (Exception e) {
//                exception2.set(e);
//            } finally {
//                latch.countDown();
//            }
//        });
//
//        latch.await();
//
//        // then
//        int conflictCount = 0;
//        if (exception1.get() instanceof OptimisticLockException) conflictCount++;
//        if (exception2.get() instanceof OptimisticLockException) conflictCount++;
//
//        assertThat(conflictCount).isEqualTo(1); // 둘 중 하나는 실패해야 정상
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void runInNewTransaction(Runnable runnable) {
//        runnable.run();
//    }
//}

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

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    runInNewTransaction(() -> bookStockService.decreaseStock(bookId, 1));
                } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                    optimisticLockFailures.incrementAndGet(); // 낙관적 락 충돌
                } catch (BadRequestException e) {
                    // 재고 부족으로 인한 예외 처리
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Book book = bookRepository.findById(bookId).orElseThrow();
        int finalStock = book.getStock();
        int totalSuccess = threadCount - optimisticLockFailures.get();

        System.out.println("낙관적 락 실패 횟수: " + optimisticLockFailures.get());
        System.out.println("최종 재고: " + finalStock);
        System.out.println("성공 요청 수: " + totalSuccess);

        // 기대: 최종 재고 + 성공 요청 수 == 초기 재고
        assertEquals(100, finalStock + totalSuccess);
        assertTrue(optimisticLockFailures.get() > 0);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runInNewTransaction(Runnable runnable) {
        runnable.run();
    }
}