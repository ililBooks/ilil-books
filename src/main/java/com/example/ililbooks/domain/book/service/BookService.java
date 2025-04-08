package com.example.ililbooks.domain.book.service;

import com.example.ililbooks.client.BookClient;
import com.example.ililbooks.client.dto.BookApiResponse;
import com.example.ililbooks.domain.book.dto.request.BookCreateRequest;
import com.example.ililbooks.domain.book.dto.request.BookUpdateRequest;
import com.example.ililbooks.domain.book.dto.response.BookResponse;
import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.repository.BookRepository;
import com.example.ililbooks.domain.user.entity.Users;
import com.example.ililbooks.domain.review.dto.response.ReviewResponse;
import com.example.ililbooks.domain.review.service.ReviewFindService;
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
    private final ReviewFindService reviewFindService;

    @Transactional
    public BookResponse createBook(AuthUser authUser, BookCreateRequest bookCreateRequest) {
        Users findUsers = userService.findByIdOrElseThrow(authUser.getUserId());

        //이미 등록된 책인 경우 (책 고유 번호로 판별)
        if(bookRepository.existsByIsbn(bookCreateRequest.getIsbn())) {
            throw new BadRequestException(DUPLICATE_BOOK.getMessage());
        }

        Book createBook = Book.of(findUsers, bookCreateRequest);
        Book savedBook = bookRepository.save(createBook);

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

            Book savedBook = Book.of(findUsers, book, price, randomStock);

            bookRepository.save(savedBook);
        }
    }

    @Transactional(readOnly = true)
    public BookResponse getBookResponse(Long bookId, int pageNum, int pageSize) {
        Book findBook = findBookByIdOrElseThrow(bookId);

        Page<ReviewResponse> reviews = reviewFindService.getReviews(findBook.getId(), pageNum, pageSize);

        return BookResponse.of(findBook, reviews);
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> getBooks(int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        Page<Book> findBooks = bookRepository.findAll(pageable);

        return ofList(findBooks);
    }

    @Transactional
    public void updateBook(Long bookId, BookUpdateRequest bookUpdateRequest) {
        Book findBook = findBookByIdOrElseThrow(bookId);

        findBook.updateBook(bookUpdateRequest);
    }

    @Transactional
    public void deleteBook(Long bookId) {
        Book findBook = findBookByIdOrElseThrow(bookId);

        bookRepository.delete(findBook);
    }

    public Book findBookByIdOrElseThrow(Long bookId) {
        return bookRepository.findById(bookId).orElseThrow(() -> new NotFoundException(NOT_FOUND_BOOK.getMessage()));
    }

    public boolean existsOnSaleRegularBookById(Long bookId) {
        return bookRepository.existsOnSaleRegularBookById(bookId);
    }
}
