package com.example.ililbooks.domain.search.service;

import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.search.entity.BookDocument;
import com.example.ililbooks.domain.search.repository.BookSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookSearchService {

    private final BookSearchRepository bookSearchRepository;


    public void saveBookDocumentFromBook(Book book) {
        BookDocument document = BookDocument.toDocument(book);
        bookSearchRepository.save(document);
    }

}
