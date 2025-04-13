package com.example.ililbooks.domain.search.repository;

import com.example.ililbooks.domain.search.entity.BookDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


public interface BookSearchRepository extends ElasticsearchRepository<BookDocument, String> {
    @Query(
            "{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^5\", \"author^3\", \"publisher^2\", \"category\"], \"type\": \"best_fields\"}}"
    )
    Page<BookDocument> findByMultiMatch(Pageable pageable, String query);
}
