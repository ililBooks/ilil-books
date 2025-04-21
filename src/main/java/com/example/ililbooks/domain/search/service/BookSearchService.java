package com.example.ililbooks.domain.search.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.repository.BookRepository;
import com.example.ililbooks.domain.search.dto.BookSearchResponse;
import com.example.ililbooks.domain.search.entity.BookDocument;
import com.example.ililbooks.domain.search.repository.BookSearchRepository;
import com.example.ililbooks.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.ililbooks.global.exception.ErrorMessage.NOT_FOUND_BOOK_DOCUMENT;

@Service
@RequiredArgsConstructor
@Transactional
public class BookSearchService {

    private final BookSearchRepository bookSearchRepository;
    private final BookRepository bookRepository;
    private final TrendingSearchService trendingSearchService;

    public void saveBookDocumentFromBook(Book book) {
        BookDocument document = BookDocument.toDocument(book);
        bookSearchRepository.save(document);
    }

    public void saveAll(List<BookDocument> bookDocuments) {
        if (bookDocuments == null) throw new NullPointerException();
        bookSearchRepository.saveAll(bookDocuments);
    }

    //TODO 검색이 빈 문자열일경우엔?
    public Page<BookSearchResponse> searchBooksV2(String keyword, Pageable pageable) {
        Page<BookDocument> bookDocuments = bookSearchRepository.findByMultiMatch(pageable, keyword);

        trendingSearchService.increaseTrendingCount(keyword);

        return bookDocuments.map(BookSearchResponse::of);
    }

    public Page<BookSearchResponse> searchBooksV1(String keyword, Pageable pageable) {
        Page<Book> books = bookRepository.findBooksByKeyword(keyword, pageable);

        trendingSearchService.increaseTrendingCount(keyword);

        return books.map(BookSearchResponse::of);
    }

    public void updateBookDocument(Book book) {
        BookDocument bookDocument = bookSearchRepository.findByIsbnOnSale(book.getIsbn())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BOOK_DOCUMENT.getMessage()));
        bookDocument.updateBookDocument(book);
    }

    public void deleteBookDocument(Book book) {
        BookDocument bookDocument = bookSearchRepository.findByIsbnOnSale(book.getIsbn())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BOOK_DOCUMENT.getMessage()));
        bookDocument.deleteBookDocument();
    }

}
