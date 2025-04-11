package com.example.ililbooks.domain.book.repository;

import com.example.ililbooks.domain.book.entity.BookImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImageBookRepository extends JpaRepository<BookImage, Long> {

    List<BookImage> findAllByBookId(Long bookId);

    Optional<BookImage> findImageById(Long imageId);

    int countByBookId(Long bookId);

    void deleteAllByBookId(Long bookId);
}
