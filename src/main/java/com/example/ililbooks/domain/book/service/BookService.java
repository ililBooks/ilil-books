package com.example.ililbooks.domain.book.service;

import com.example.ililbooks.domain.book.dto.request.BookCreateRequest;
import com.example.ililbooks.domain.book.dto.request.BookUpdateRequest;
import com.example.ililbooks.domain.book.dto.response.BookResponse;
import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.repository.BookRepository;
import com.example.ililbooks.domain.user.entity.User;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.ililbooks.global.exception.ErrorMessage.NOT_FOUND_BOOK;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final UserService userService;

    @Transactional
    public BookResponse createBook(AuthUser authUser, BookCreateRequest bookCreateRequest) {

        User findUser = userService.getUserById(authUser.getUserId());

        //todo: openAPI 추가 후 고유번호로 이미 등록된 책인지 아닌지 판별할 예정

        Book savedBook = Book.builder()
                .user(findUser)
                .title(bookCreateRequest.getTitle())
                .author(bookCreateRequest.getAuthor())
                .price(bookCreateRequest.getPrice())
                .category(bookCreateRequest.getCategory())
                .stock(bookCreateRequest.getStock())
                .build();

        bookRepository.save(savedBook);

        return BookResponse.of(savedBook);
    }

    @Transactional(readOnly = true)
    public BookResponse getBookResponse(Long bookId) {
        Book findBook = getBookById(bookId);

        return BookResponse.of(findBook);
    }

    @Transactional
    public void updateBook(Long bookId, BookUpdateRequest bookUpdateRequest) {
        Book findBook = getBookById(bookId);

        findBook.updateBook(bookUpdateRequest);
    }

    @Transactional
    public void deleteBook(Long bookId) {
        Book findBook = getBookById(bookId);

        bookRepository.delete(findBook);
    }

    public Book getBookById(Long bookId) {
        return bookRepository.findById(bookId).orElseThrow(() -> new NotFoundException(NOT_FOUND_BOOK.getMessage()));
    }
}
