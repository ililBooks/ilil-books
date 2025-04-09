package com.example.ililbooks.domain.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.example.ililbooks.domain.book.entity.Book;
import com.example.ililbooks.domain.search.entity.BookDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class BookIndexer {

    private final ElasticsearchClient elasticsearchClient;


    public void save(Book book) throws IOException {
        BookDocument document = BookDocument.toDocument(book);
        elasticsearchClient.index(i ->
                i.index("books")
                        .id(document.getId())
                        .document(document)
        );
    }
}
