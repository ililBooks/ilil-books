package com.example.ililbooks.domain.search.repository;

import com.example.ililbooks.domain.search.entity.BookDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BookSearchRepository extends ElasticsearchRepository<BookDocument, String> {
    Page<BookDocument> findByTitleContainingAndAuthorContaining(Pageable pageable, String keyword, int page, int size);
}
