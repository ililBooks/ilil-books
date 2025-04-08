package com.example.ililbooks.domain.book.service;

import com.example.ililbooks.client.BookClient;
import com.example.ililbooks.client.dto.BookApiResponse;
import com.example.ililbooks.domain.book.dto.request.BookCreateRequest;
import com.example.ililbooks.domain.book.dto.request.BookUpdateRequest;
import com.example.ililbooks.domain.book.dto.response.BookResponse;
import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.repository.BookRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.user.service.UserService;
import com.example.ililbooks.global.dto.AuthUser;
import com.example.ililbooks.global.exception.BadRequestException;
import com.example.ililbooks.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Random;

import static com.example.ililbooks.domain.book.dto.response.BookResponse.ofList;
import static com.example.ililbooks.global.exception.ErrorMessage.*;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final UserService userService;
    private final BookClient bookClient;

    @Transactional
    public BookResponse createBook(AuthUser authUser, BookCreateRequest bookCreateRequest) {
        Users findUsers = userService.findByIdOrElseThrow(authUser.getUserId());

        //이미 등록된 책인 경우 (책 고유 번호로 판별)
        if(bookRepository.existsByIsbn(bookCreateRequest.getIsbn())) {
            throw new BadRequestException(DUPLICATE_BOOK.getMessage());
        }

        Book savedBook = Book.builder()
                .users(findUsers)
                .title(bookCreateRequest.getTitle())
                .author(bookCreateRequest.getAuthor())
                .price(bookCreateRequest.getPrice())
                .category(bookCreateRequest.getCategory())
                .stock(bookCreateRequest.getStock())
                .isbn(bookCreateRequest.getIsbn())
                .build();

        bookRepository.save(savedBook);

        return BookResponse.of(savedBook);
    }

    @Transactional
    public void createBookByOpenApi(AuthUser authUser, Integer pageNum, Integer pageSize) {

        Users findUsers = userService.findByIdOrElseThrow(authUser.getUserId());

        //open api를 통해 책 리스트 가져오기
        List<BookApiResponse> books = List.of(bookClient.getBooks(findUsers.getNickname(), pageNum, pageSize));

        //랜덤 가격 및 재고 생성을 위한 Random객체 선언
        Random random = new Random();

        for (BookApiResponse book : books) {
            //랜덤 가격 (Min: 5000, Max: 45000)
            long randomPrice = 5000 + random.nextLong(40000);
            Long price = Math.round(randomPrice / 1000.0) * 1000;

            //랜덤 재고 (Min: 1, Max:101)
            int randomStock = 1 +  random.nextInt(100);

            //책 고유번호가 없는 경우
            if (!StringUtils.hasText(book.getIsbn())) {
                throw new BadRequestException(BOOK_ISBN_MISSING.getMessage());
            }

            //이미 등록된 책인 경우 저장하지 않음
            if (bookRepository.existsByIsbn(book.getIsbn())) {
                continue;
            }

            Book savedBook = Book.builder()
                    .users(findUsers)
                    .title(book.getTitle())
                    .author(book.getAuthor().replaceAll("<[^>]*>", ""))
                    .price(price)
                    .category(book.getCategory())
                    .stock(randomStock)
                    .isbn(book.getIsbn())
                    .build();

            bookRepository.save(savedBook);
        }
    }

    @Transactional(readOnly = true)
    public BookResponse getBookResponse(Long bookId) {
        Book findBook = getBookById(bookId);

        return BookResponse.of(findBook);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getBooks(int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        return ofList(bookRepository.findAll(pageable));
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

    public boolean existsOnSaleRegularBookById(Long bookId) {
        return bookRepository.existsOnSaleRegularBookById(bookId);
    }
}
