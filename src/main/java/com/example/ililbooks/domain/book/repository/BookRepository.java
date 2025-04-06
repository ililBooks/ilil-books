package com.example.ililbooks.domain.book.repository;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByIsbn(String isbn);

    List<Book> user(User user);
}
