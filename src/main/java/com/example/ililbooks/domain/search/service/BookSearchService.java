package com.example.ililbooks.domain.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.search.dto.BookSearchResponse;
import com.example.ililbooks.domain.search.entity.BookDocument;
import com.example.ililbooks.domain.search.repository.BookSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookSearchService {

    private final BookSearchRepository bookSearchRepository;
    private final ElasticsearchClient elasticsearchClient;

    public void saveBookDocumentFromBook(Book book) {
        BookDocument document = BookDocument.toDocument(book);
        bookSearchRepository.save(document);
//        elasticsearchClient.index()
    }

    public Page<BookSearchResponse> searchBooks(String keyword, int page, int size) throws IOException {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<BookDocument> bookDocuments = bookSearchRepository.findByTitleContainingAndAuthorContaining(pageable, keyword, page, size);

//        SearchResponse<BookDocument> response = elasticsearchClient.search(
//                s -> s
//                        .index("books")
//                        .query(q -> q
//                                .match(m -> m
//                                        .field("title")
//                                        .query(keyword)
//                                )
//                        ),
//                BookDocument.class
//        );


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
}
