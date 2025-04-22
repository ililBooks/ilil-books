package com.example.ililbooks.domain.book.repository;

import com.example.ililbooks.domain.book.entity.BookImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImageBookRepository extends JpaRepository<BookImage, Long> {

    List<BookImage> findAllByBookId(Long bookId);

    @Query(value = "SELECT * FROM ililbooks.book_images WHERE book_id = :bookId ORDER BY position_index LIMIT 1", nativeQuery = true)
    Optional<BookImage> findFirstByBookId(@Param("bookId") Long bookId);

    boolean existsByBookIdAndPositionIndex(Long bookId, int positionIndex);

    Optional<BookImage> findImageById(Long imageId);

    int countByBookId(Long bookId);

    void deleteAllByBookId(Long bookId);
}
