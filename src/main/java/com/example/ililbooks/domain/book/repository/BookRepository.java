package com.example.ililbooks.domain.book.repository;

import com.example.ililbooks.domain.book.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByIsbn(String isbn);

    @Query("SELECT COUNT(b) > 0 FROM Book b " +
            "WHERE b.id = :bookId " +
            "AND b.saleStatus = 'ON_SALE' " +
            "AND b.limitedType = 'REGULAR'")
    boolean existsOnSaleRegularBookById(@Param("bookId") Long bookId);

    @Query("SELECT b FROM Book b WHERE b.id = :bookId AND b.isDeleted = false")
    Optional<Book> findBookById(@Param("bookId") Long bookId);

    @Query("SELECT b FROM Book b WHERE b.isDeleted = false")
    Page<Book> findAllNotDeleted(Pageable pageable);
}
