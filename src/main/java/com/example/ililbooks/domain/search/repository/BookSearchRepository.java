package com.example.ililbooks.domain.search.repository;

import com.example.ililbooks.domain.search.entity.BookDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

public interface BookSearchRepository extends ElasticsearchRepository<BookDocument, String> {
}
