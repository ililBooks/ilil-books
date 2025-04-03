package com.example.ililbooks.domain.book.repository;

import com.example.ililbooks.domain.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
