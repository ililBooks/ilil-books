package com.example.ililbooks.domain.book.service;

import com.example.ililbooks.client.BookClient;
import com.example.ililbooks.client.dto.BookApiResponse;
import com.example.ililbooks.domain.book.dto.request.BookUpdateRequest;
import com.example.ililbooks.domain.book.dto.response.BookResponse;
import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.repository.BookRepository;
import com.example.ililbooks.domain.user.entity.User;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.example.ililbooks.domain.book.dto.response.BookResponse.ofList;
import static com.example.ililbooks.global.exception.ErrorMessage.NOT_FOUND_BOOK;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final UserService userService;
    private final BookClient bookClient;

    @Transactional
    public List<BookResponse> createBook(AuthUser authUser, Integer pageNum, Integer pageSize) {

        User findUser = userService.getUserById(authUser.getUserId());

        List<BookApiResponse> books = Arrays.stream(bookClient.getBooks(pageNum, pageSize)).toList();

        Random random = new Random();

        for (BookApiResponse book : books) {
            //랜덤 가격, 수량 저장
            Long randomPrice = 5000 + random.nextLong(15000);
            int randomStock = 1 + random.nextInt(100);

            //이미 등록된 책인 경우 저장하지 않음
            if(bookRepository.existsByIsbn(book.getIsbn())) {
                continue;
            }

            Book savedBook = Book.builder()
                    .user(findUser)
                    .title(book.getTitle())
                    .author(book.getAuthor())
                    .price(randomPrice)
                    .category(book.getCategory())
                    .stock(randomStock)
                    .isbn(book.getIsbn())
                    .build();

            bookRepository.save(savedBook);
        }

        PageRequest pageable = PageRequest.of(pageNum , 10);
        Page<Book> findBooks = bookRepository.findAll(pageable);

        return ofList(findBooks);
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
