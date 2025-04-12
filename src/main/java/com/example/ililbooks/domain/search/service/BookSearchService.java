package com.example.ililbooks.domain.search.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.book.repository.BookRepository;
import com.example.ililbooks.domain.search.dto.BookSearchResponse;
import com.example.ililbooks.domain.search.entity.BookDocument;
import com.example.ililbooks.domain.search.repository.BookSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookSearchService {

    private final BookSearchRepository bookSearchRepository;
    private final BookRepository bookRepository;


    public void saveBookDocumentFromBook(Book book) {
        BookDocument document = BookDocument.toDocument(book);
        bookSearchRepository.save(document);
    }

    public void saveAll(List<BookDocument> bookDocuments) { bookSearchRepository.saveAll(bookDocuments);}

    public Page<BookSearchResponse> searchBooksV2(String keyword, int page, int size){
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<BookDocument> bookDocuments = bookSearchRepository.findByMultiMatch(pageable, keyword);

        return bookDocuments.map(doc ->
                BookSearchResponse.builder()
                        .title(doc.getTitle())
                        .author(doc.getAuthor())
                        .publisher(doc.getPublisher())
                        .category(doc.getCategory())
                        .price(doc.getPrice())
                        .saleStatus(doc.getSaleStatus())
                        .limitedType(doc.getLimitedType())
                        .build()
        );
    }

    public Page<BookSearchResponse> searchBooks(String keyword, int page, int size){
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Book> books = bookRepository.findBooksByKeyword(keyword, pageable);

        return books.map(book ->
                BookSearchResponse.builder()
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .category(book.getCategory())
                .price(book.getPrice())
                .saleStatus(book.getSaleStatus().name())
                .limitedType(book.getLimitedType().name())
                .build()
        );
    }
}
