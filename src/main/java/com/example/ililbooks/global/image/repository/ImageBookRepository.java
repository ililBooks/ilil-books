package com.example.ililbooks.global.image.repository;

import com.example.ililbooks.global.image.entity.BookImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ImageBookRepository extends JpaRepository<BookImage, Long> {
    boolean existsByBookId(Long bookId);

    List<BookImage> findAllByBookId(Long bookId);
}
