package com.example.ililbooks.domain.book.repository;

import com.example.ililbooks.domain.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByIsbn(String isbn);

    @Query("SELECT COUNT(b) > 0 FROM Book b " +
            "WHERE b.id = :bookId " +
            "AND b.saleStatus = 'ON_SALE' " +
            "AND b.limitedType = 'REGULAR'")
    boolean existsOnSaleRegularBookById(@Param("bookId") Long bookId);

}
